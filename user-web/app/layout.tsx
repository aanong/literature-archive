import type { Metadata } from "next";
import "./globals.css";
import { Navbar } from "@/components/Navbar";
import { AiChatWidget } from "@/components/AiChatWidget";

export const metadata: Metadata = {
  title: "云章·经籍",
  description: "承道以文 · 鉴古知今",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="zh-CN">
      <body suppressHydrationWarning>
        <div className="relative flex min-h-screen flex-col font-serif">
          <Navbar />
          <main className="flex-1">{children}</main>
          <footer className="py-6 md:px-8 md:py-0 border-t border-border/40 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
            <div className="container flex flex-col items-center justify-between gap-4 md:h-24 md:flex-row">
              <p className="text-balance text-center text-sm leading-loose text-muted-foreground md:text-left font-serif opacity-80">
                © 2026 云章书院 · 承道以文
              </p>
            </div>
          </footer>
        </div>
        <AiChatWidget />
      </body>
    </html>
  );
}
