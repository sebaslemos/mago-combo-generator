import { Markdown, MessageInput, MessageList } from '@vaadin/react-components';
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
  const [fichaMarkdown, setFichaMarkdown] = useState<string>('');

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
    const tokens = resposta.split('---');
    setMessages((prevMessages) => [
      ...prevMessages,
      {
        text: tokens[0],
        userName: 'Gemini',
        userColorIndex: 1,
      },
    ]);

    if (tokens.length > 1) {
      setFichaMarkdown(tokens[1]);
    } else {
      setFichaMarkdown('');
    }
  }

  return (
    <div className="container-pai">
      <div className="coluna-metade">
        <div className="message-list-scroll-container">
          <MessageList items={messages} markdown />
        </div>
        <MessageInput onSubmit={handleInput}></MessageInput>
      </div>
      <div className="coluna-metade">
        <Markdown>{fichaMarkdown}</Markdown>
      </div>
    </div>
  );
}
