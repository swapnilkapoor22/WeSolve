import { render, screen } from "@testing-library/react";
import { Provider } from "react-redux";
import { App } from "./App";
import { store } from "./store";

test("shows the sign-in screen", () => {
  render(
    <Provider store={store}>
      <App />
    </Provider>,
  );
  expect(screen.getByRole("heading", { name: "Sign in" })).toBeTruthy();
});
