#include <algorithm>
#include <arpa/inet.h>
#include <cmath>
#include <netinet/in.h>
#include <sstream>
#include <string>
#include <sys/socket.h>
#include <unistd.h>
#include <vector>

double historical_var95(const std::vector<double>& pnl) {
    if (pnl.empty()) return 0.0;
    std::vector<double> sorted = pnl;
    std::sort(sorted.begin(), sorted.end());
    size_t idx = static_cast<size_t>(std::floor(0.05 * sorted.size()));
    if (idx >= sorted.size()) idx = sorted.size() - 1;
    double loss = sorted[idx];
    return std::abs(loss);
}

std::vector<double> parse_pnl_array(const std::string& body) {
    std::vector<double> values;
    auto start = body.find('[');
    auto end = body.find(']');
    if (start == std::string::npos || end == std::string::npos || end <= start) return values;
    std::string array = body.substr(start + 1, end - start - 1);
    std::stringstream ss(array);
    std::string item;
    while (std::getline(ss, item, ',')) {
        try {
            values.push_back(std::stod(item));
        } catch (...) {
        }
    }
    return values;
}

std::string build_response(double var) {
    std::ostringstream oss;
    oss << "{ \"var95\": " << var << " }";
    std::string json = oss.str();
    std::ostringstream resp;
    resp << "HTTP/1.1 200 OK\r\n"
         << "Content-Type: application/json\r\n"
         << "Content-Length: " << json.size() << "\r\n"
         << "Connection: close\r\n\r\n"
         << json;
    return resp.str();
}

int main() {
    int server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_fd == -1) {
        perror("socket");
        return 1;
    }

    int opt = 1;
    setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &opt, sizeof(opt));

    sockaddr_in address{};
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = INADDR_ANY;
    address.sin_port = htons(8090);

    if (bind(server_fd, reinterpret_cast<sockaddr*>(&address), sizeof(address)) < 0) {
        perror("bind");
        close(server_fd);
        return 1;
    }
    if (listen(server_fd, 10) < 0) {
        perror("listen");
        close(server_fd);
        return 1;
    }

    while (true) {
        socklen_t addrlen = sizeof(address);
        int client_fd = accept(server_fd, reinterpret_cast<sockaddr*>(&address), &addrlen);
        if (client_fd < 0) {
            perror("accept");
            continue;
        }

        std::string request;
        char buffer[2048];
        ssize_t bytes;
        while ((bytes = read(client_fd, buffer, sizeof(buffer))) > 0) {
            request.append(buffer, buffer + bytes);
            if (request.find("\r\n\r\n") != std::string::npos) {
                break;
            }
        }

        auto content_pos = request.find("Content-Length:");
        size_t content_length = 0;
        if (content_pos != std::string::npos) {
            std::stringstream cl(request.substr(content_pos + 15));
            cl >> content_length;
        }
        auto body_pos = request.find("\r\n\r\n");
        std::string body = body_pos != std::string::npos ? request.substr(body_pos + 4) : "";
        while (body.size() < content_length) {
            bytes = read(client_fd, buffer, sizeof(buffer));
            if (bytes <= 0) break;
            body.append(buffer, buffer + bytes);
        }

        auto pnl = parse_pnl_array(body);
        double var = historical_var95(pnl);
        std::string response = build_response(var);
        send(client_fd, response.c_str(), response.size(), 0);
        close(client_fd);
    }
}
