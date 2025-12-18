export interface ChatMessage {
  id: string;
  chatId: string;
  chatMemberId: string;
  content: string;
  message: string;      // Optional UI binding
  messageType: 'TEXT' | 'IMAGE' | 'FILE' | 'OTHER';
  createdAt: string;    // ISO string
  updatedAt?: string;
  isRead: boolean;
  isEdited: boolean;
  isDeleted: boolean;
  userId: string;
  fileUrl?: string; // For IMAGE or OTHER types
  fileName?: string;
  name: string;
  align?: 'right' | 'left'; // Optional UI alignment
  chatMember?: {
    id: string;
    user: {
      id: string;
      name?: string;
      image?: string;
    };
  };
}

// Deprecated: use ChatMessageDTO instead of ChatMessage
export interface ChatUser {
  id: string;
  chatMemberId?: string;
  name: string;
  message?: string;
  time?: string;
  color?: string;
  image?: string;
  status?: 'online' | 'offline';
}

export interface loadMessages {
  messageId: string;
  content: string;
  createdAt: string;
  senderId: string;
  senderName: string;
  messageType: 'TEXT' | 'IMAGE' | 'FILE' | 'OTHER';
  fileUrl?: string;
  fileName?: string;
}