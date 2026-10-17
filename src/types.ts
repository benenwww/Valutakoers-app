export interface DepositItem {
  id: string;
  name: string;
  value: number;
  count: number;
  description: string;
  unitText: string;
}

export interface DepositSession {
  items: Record<string, number>;
  updatedAt: string;
  storeName?: string;
}
