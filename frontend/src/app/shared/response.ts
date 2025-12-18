export interface GlobalResponse<T> {
  status: string;
  data: T | null;
  errors: { message: string }[] | null;
}