export type Role = 'ADMIN' | 'SALES' | 'OVERSEAS_ORDER' | 'WAREHOUSE' | 'SUPPLIER';
export type AccountStatus = 'PENDING' | 'APPROVED' | 'BLOCKED';

export interface User {
  id: string;
  email: string;
  fullName: string;
  employeeId: string;
  role: Role;
  status: AccountStatus;
}

export interface AuthResponse {
  access_token: string;
  user: User;
}

export interface LoginData {
  email: string;
  password: string;
}

export interface RegisterData {
  fullName: string;
  email: string;
  password: string;
  employeeId: string;
  role: Role;
}
