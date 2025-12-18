// usersearchresponse.model.ts

export interface UserSearchResponse {
  id: string;
  username: string;          // backend field
  email?: string;
  profilePictureUrl?: string;
}


export interface FrontendUser {
  id: string;
  name: string;   // mapped from username
  image: string | null; // mapped from profilePictureUrl
}