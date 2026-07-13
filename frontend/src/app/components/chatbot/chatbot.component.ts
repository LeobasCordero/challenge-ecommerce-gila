import { Component, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatbotService } from '../../core/api/api/chatbot.service';
import { AuthStateService } from '../../services/auth-state.service';

interface Message {
  sender: 'user' | 'bot';
  text: string;
  time: Date;
}

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chatbot.component.html',
})
export class ChatbotComponent {
  private readonly chatbotService = inject(ChatbotService);
  public readonly authState = inject(AuthStateService);

  @ViewChild('messageContainer') private messageContainer!: ElementRef;

  public isOpen = signal(false);
  public isLoading = signal(false);
  public userMessage = '';
  
  public messages = signal<Message[]>([
    {
      sender: 'bot',
      text: 'Hello! I am your e-commerce AI assistant. Ask me questions about product catalog metrics or today\'s sales.',
      time: new Date()
    }
  ]);

  public toggleChat(): void {
    this.isOpen.update(val => !val);
    if (this.isOpen()) {
      setTimeout(() => this.scrollToBottom(), 100);
    }
  }

  public sendMessage(text?: string): void {
    const messageToSend = text || this.userMessage;
    if (!messageToSend || !messageToSend.trim()) {
      return;
    }

    if (!text) {
      this.userMessage = '';
    }

    // Append user message
    this.messages.update(msgs => [
      ...msgs,
      { sender: 'user', text: messageToSend, time: new Date() }
    ]);
    setTimeout(() => this.scrollToBottom(), 50);

    this.isLoading.set(true);

    this.chatbotService.queryChatbot({ message: messageToSend }).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        this.messages.update(msgs => [
          ...msgs,
          { sender: 'bot', text: response.reply, time: new Date() }
        ]);
        setTimeout(() => this.scrollToBottom(), 50);
      },
      error: (err) => {
        this.isLoading.set(false);
        let errorMsg = 'An error occurred while connecting to the chatbot service.';
        if (err.status === 400 && err.error?.reply) {
          errorMsg = err.error.reply;
        } else if (err.status === 403) {
          errorMsg = 'Access Denied: Enforced by policies.';
        }
        this.messages.update(msgs => [
          ...msgs,
          { sender: 'bot', text: errorMsg, time: new Date() }
        ]);
        setTimeout(() => this.scrollToBottom(), 50);
      }
    });
  }

  private scrollToBottom(): void {
    try {
      if (this.messageContainer) {
        this.messageContainer.nativeElement.scrollTop = this.messageContainer.nativeElement.scrollHeight;
      }
    } catch {
      // Ignore scroll failures
    }
  }
}
