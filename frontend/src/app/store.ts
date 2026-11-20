import { configureStore } from "@reduxjs/toolkit";

export const store = configureStore({
  reducer: {
    session: (state: { ready: boolean } = { ready: true }) => state,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
