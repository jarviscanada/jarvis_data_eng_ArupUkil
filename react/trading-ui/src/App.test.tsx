import { render, screen } from "@testing-library/react";
import App from "./App";

test("renders dashboard route", async () => {
  render(<App />);
  expect(await screen.findByText("Dashboard")).toBeInTheDocument();
});
