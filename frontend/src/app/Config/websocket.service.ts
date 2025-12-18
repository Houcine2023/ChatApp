import { Injectable } from '@angular/core';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  private client: Client;
  private subscriptions: Map<string, StompSubscription> = new Map();

  constructor() {
    this.client = new Client({
      // WebSocket endpoint of your Spring Boot backend
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),

      // Reconnect options
      reconnectDelay: 5000,

      // Debug logs
      debug: (str) => {
        console.log(str);
      }
    });

    // Optional: connect immediately
    this.client.activate();
  }

  // Subscribe to a topic
  subscribe(destination: string, callback: (message: IMessage) => void): void {
    if (!this.client.active) {
      console.warn('STOMP client not active yet');
      return;
    }

    const sub = this.client.subscribe(destination, callback);
    this.subscriptions.set(destination, sub);
  }

  // Unsubscribe from a topic
  unsubscribe(destination: string): void {
    const sub = this.subscriptions.get(destination);
    if (sub) {
      sub.unsubscribe();
      this.subscriptions.delete(destination);
    }
  }

  // Send message to backend
  send(destination: string, body: any): void {
    if (this.client.connected) {
      this.client.publish({
        destination,
        body: JSON.stringify(body)
      });
    } else {
      console.error('STOMP client not connected yet.');
    }
  }

  // Disconnect client
  disconnect(): void {
    this.client.deactivate();
  }
}
