export type UserRole = 'ADMIN' | 'ANALYST';

export interface UserRequest {
  name: string;
  email: string;
  password: string;
  role: UserRole;
}

export interface UserResponse {
  id: string;
  name: string;
  email: string;
  role: UserRole;
  createdAt: string;
}

export interface UserUpdate {
  name?: string;
  email?: string;
  password?: string;
  role?: UserRole;
}
