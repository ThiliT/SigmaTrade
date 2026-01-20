# Real-Time Portfolio, Trading, and Risk Demo

Educational single-developer stack showcasing microservices, real-time price simulation, order handling, and lightweight risk analytics.

## Modules
- `backend/common` — shared DTOs and enums.
- `backend/market-data-service` — simulates prices every 5s, stores history, exposes REST and SSE.
- `backend/portfolio-service` — portfolios, holdings, valuations, P/L.
- `backend/order-service` — market-only buy/sell validation and portfolio updates.
- `backend/risk-service` — exposure, volatility, VaR (delegated to C++ engine).
- `cpp-risk-engine` — minimal C++17 HTTP server computing historical-simulation VaR.
- `frontend/` — simple HTML dashboard calling the services.

## Running with Docker Compose
```bash
docker compose build
docker compose up
```
Services:
- Market data: http://localhost:8081
- Portfolio: http://localhost:8082
- Orders: http://localhost:8083
- Risk: http://localhost:8084
- C++ VaR engine: http://localhost:8090
- Frontend: open `frontend/index.html` in a browser (served locally or via `python -m http.server 3000`).

## Example Requests
- Latest price: `GET http://localhost:8081/prices/AAPL`
- Stream prices: `GET http://localhost:8081/prices/stream`
- Create portfolio:
  ```json
  POST http://localhost:8082/portfolios
  { "name": "Sample", "cashBalance": 100000 }
  ```
- Place order (market):
  ```json
  POST http://localhost:8083/orders
  { "portfolioId": 1, "symbol": "AAPL", "side": "BUY", "quantity": 5 }
  ```
- VaR: `GET http://localhost:8084/risk/var/1`

## Risk Methodology (simplified)
- Historical VaR 95%: aggregate P/L paths from recent price changes per holding, delegate to C++ engine which returns the 5th percentile loss (absolute value).
- Exposure: quantity × latest price per asset.
- Volatility: sample standard deviation of daily returns from 30-day history.

## Design Notes & Limitations
- REST only; WebClient for inter-service calls.
- No auth, no message broker, no tracing by design.
- Price generation is stochastic and educational only.
- C++ server is intentionally minimal (single-threaded, naive JSON parsing).
- Persistence uses auto DDL; per-service Postgres instances in Compose.
- Validation is basic; production concerns (retries, idempotency, money math) are out of scope.

## Future Work
- Add proper JSON parsing and structured logging in the C++ engine.
- Add tests per service.
- Replace blocking WebClient calls with reactive flows where helpful.
- Serve the frontend from a static container.
