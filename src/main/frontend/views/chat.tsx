import { Markdown, MessageInput, MessageList } from '@vaadin/react-components';
import { GeminiChatService } from 'Frontend/generated/endpoints';
import { useState, useEffect, useRef } from 'react';

export default function ChatView() {
  type Message = {
    text: string;
    userName: string;
    userColorIndex: number;
    theme?: string;
  };

  const [messages, setMessages] = useState<Message[]>([]);
  const [fichaMarkdown, setFichaMarkdown] = useState<string>('');
  const messageListRef = useRef<HTMLDivElement>(null);
  const [isWaiting, setIsWaiting] = useState<boolean>(false);

  useEffect(() => {
    if (messageListRef.current) {
      messageListRef.current.scrollTop = messageListRef.current.scrollHeight;
    }
  }, [messages]);

  async function handleInput(event: CustomEvent) {
    setIsWaiting(true);
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
    setIsWaiting(false);

    if (tokens.length > 1) {
      setFichaMarkdown(tokens[1]);
    } else {
      setFichaMarkdown('');
    }
  }

  return (
    <div className="container-pai">
      <div className="coluna-metade">
        <div className="message-list-scroll-container" ref={messageListRef}>
          <MessageList items={messages} markdown />
        </div>
        <MessageInput onSubmit={handleInput} disabled={isWaiting}></MessageInput>
      </div>
      <div className="coluna-metade coluna-ficha">
        <h3 className="coluna-titulo">Ficha do Personagem</h3>
        <div className="markdown-scroll-container">
          <Markdown>{fichaMarkdown}</Markdown>
        </div>
      </div>
    </div>
  );
}
