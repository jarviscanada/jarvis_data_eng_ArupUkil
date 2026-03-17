export type Id = number;

export interface Trader {
  id: Id;
  firstName: string;
  lastName: string;
  email: string;
  country: string;
  dob: string;
}

export interface TraderAccountView {
  traderId: Id;
  firstName: string;
  lastName: string;
  dob: string;
  country: string;
  email: string;
  accountId: Id;
  amount: number | null;
}

export interface Quote {
  ticker: string;
  lastPrice: number | null;
  bidPrice: number | null;
  bidSize: number | null;
  askPrice: number | null;
  askSize: number | null;
}
