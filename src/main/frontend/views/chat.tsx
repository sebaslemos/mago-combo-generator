import { MessageInput, MessageList } from '@vaadin/react-components';
import { GeminiChatService } from 'Frontend/generated/endpoints';
import { useState } from 'react';

export default function ChatView() {
  type Message = {
    text: string;
    userName: string;
    userColorIndex: number;
    theme?: string;
  };

  const [messages, setMessages] = useState<Message[]>([]);

  async function handleInput(event: CustomEvent) {
    const pergunta: string = event.detail.value;
    setMessages([
      ...messages,
      {
        text: pergunta,
        userName: 'Arthur',
        userColorIndex: 2,
      },
    ]);

    const resposta = await GeminiChatService.conversar(event.detail.value);
    setMessages((prevMessages) => [
      ...prevMessages,
      {
        text: resposta,
        userName: 'Gemini',
        userColorIndex: 1,
      },
    ]);
  }

  return (
    <>
      <MessageList items={messages} markdown />
      <MessageInput onSubmit={handleInput}></MessageInput>
    </>
  );
}
