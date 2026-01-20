FROM ubuntu:22.04 AS build
RUN apt-get update && apt-get install -y build-essential cmake
WORKDIR /src
COPY . .
RUN cmake -S . -B build && cmake --build build

FROM ubuntu:22.04
WORKDIR /app
COPY --from=build /src/build/cpp-risk-engine /app/cpp-risk-engine
EXPOSE 8090
ENTRYPOINT ["/app/cpp-risk-engine"]
