# Introduction
This project is a web dashboard for viewing and interacting with the Spring Boot trading app backend [[GitHub](https://github.com/jarviscanada/jarvis_data_eng_ArupUkil/tree/master/springboot)]. The dashboard is a user friendly way to manage traders and accounts, store quotes, and submit market orders without calling REST endpoints manually. The frontend is built with React and TypeScript, using React Router for navigation, Ant Design for consistent UI components (tables, modals, forms), and Axios for HTTP requests. The dashboard can be run locally using `npm` or through a Docker container. The backend is a Spring Boot service that exposes endpoints for trader management, quote listing, and account funding operations. The UI reads the backend base URL from `REACT_APP_BACKEND_URL` (defaulting to `http://localhost:8080`) so it can be pointed at a local or Dockerized Spring Boot instance or non-Spring Boot backend assuming the API is structured the same as the Spring Boot app.

# Quick Start
Prerequisites:
- Docker (to run the backend + database)
- Node.js + npm (to run the frontend locally)

Backend (Docker):
```bash
# from repo root
cd springboot

# build backend image (Spring Boot)
docker build -t trading-app:local .

# build database image (Postgres schema + seed)
docker build -t trading-psql:local ./psql

# create a network so containers can communicate
docker network create trading-net || true

# start database (creates DB + tables from init.sql)
docker run --rm -d --name trading-psql --network trading-net \
  -e POSTGRES_DB=trading_app \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  trading-psql:local

# start backend (replace FINNHUB token)
docker run --rm -d --name trading-app --network trading-net -p 8080:8080 \
  -e PSQL_URL=jdbc:postgresql://trading-psql:5432/trading_app \
  -e PSQL_USER=postgres \
  -e PSQL_PASSWORD=password \
  -e FINNHUB_TOKEN=YOUR_TOKEN_HERE \
  trading-app:local
```

Frontend (local dev):
```bash
cd react/trading-ui

# point the UI at your backend (optional)
export REACT_APP_BACKEND_URL="http://localhost:8080"

npm install
npm start
```

Frontend (Docker):
```bash
cd react/trading-ui

# build a production image
docker build --build-arg REACT_APP_BACKEND_URL="http://localhost:8080" -t trading-ui:local .

# serve on http://localhost:3000
docker run --rm -p 3000:80 trading-ui:local
```

# Implementation
The UI is organized into pages and reusable components:
- Pages: `Dashboard`, `Quotes`, and `Trader Account`
- Components: `NavBar` for navigation and `TraderList` for the traders table

Users interact with the application through three main pages:
- `Dashboard`: shows the full trader list, lets users add a new trader through a modal form, delete an existing trader, and click a trader row to open that trader's account page.
- `Quotes`: shows the daily quote table, including cached market data for each ticker, and lets users add a new quote entry by submitting a ticker symbol.
- `Trader Account`: shows the selected trader's profile and current account balance, and lets users deposit funds or withdraw funds through validated modal forms.

Ant Design forms are used for frontend validation (required fields, email format, and positive numeric amount checks). Errors are surfaced with toast messages for quick iteration during testing.

## Architecture
![Architecture Diagram](docs/architecture.svg)

# Test
Frontend:
```bash
cd react/trading-ui
npm test -- --watchAll=false
npm run build
```

Running `npm test` executes the frontend Jest and React Testing Library test suite. The current tests are lightweight and act as a rendering/routing check to verify that the React app mounts successfully and that the default dashboard view loads as expected. The test setup also mocks browser APIs used by Ant Design, such as `matchMedia`, so the UI can be tested reliably outside a real browser.

Manual testing in the browser has been also done by performing operations such as creating a trader and confirming it appeared in the dashboard table, and opening a trader account page to test deposit and withdrawal actions.

# Deployment
Both the frontend and backend source code is on GitHub so both services can be downloaded and run locally as shown above. They are both also containerised hence can be deployed via Docker containers. The only service not given is a Finnhub API token which you have to setup yourself. Currently the frontend hasn't been tested on the ability to be hosted through Azure unlike the backend.

# Improvements
- Deploy the frontend to Azure so the full app is hosted end-to-end with the existing backend deployment.
- Add trader account history so users can see previous deposits, withdrawals, and order activity.
- Add login page with authentication/authorization so not every user can access every trader account.
