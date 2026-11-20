import { Component, type ReactNode } from "react";
import { AppRouter } from "./router";

type BoundaryState = { failed: boolean };

class RouteErrorBoundary extends Component<{ children: ReactNode }, BoundaryState> {
  state: BoundaryState = { failed: false };

  static getDerivedStateFromError(): BoundaryState {
    return { failed: true };
  }

  render() {
    if (this.state.failed) {
      return <p role="alert">Something went wrong.</p>;
    }
    return this.props.children;
  }
}

export function App() {
  return (
    <RouteErrorBoundary>
      <AppRouter />
    </RouteErrorBoundary>
  );
}
