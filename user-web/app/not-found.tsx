import Link from "next/link";
import { Home, BookOpen } from "lucide-react";

export default function NotFound() {
  return (
    <div className="container flex flex-col items-center justify-center min-h-[60vh] py-12 font-serif">
      <div className="text-center space-y-6">
        <div className="text-8xl opacity-30">📜</div>
        
        <h1 className="text-4xl font-bold text-primary tracking-wider">
          此页无迹可寻
        </h1>
        
        <p className="text-lg text-muted-foreground max-w-md mx-auto">
          所求之页，或已湮没于岁月长河，或从未存世。
        </p>

        <div className="flex flex-wrap justify-center gap-4 pt-6">
          <Link
            href="/"
            className="inline-flex items-center gap-2 px-6 py-3 rounded-md bg-primary text-primary-foreground hover:bg-primary/90 transition-colors"
          >
            <Home className="h-4 w-4" />
            返回首页
          </Link>
          <Link
            href="/library"
            className="inline-flex items-center gap-2 px-6 py-3 rounded-md border border-border hover:bg-accent transition-colors"
          >
            <BookOpen className="h-4 w-4" />
            探寻藏书
          </Link>
        </div>
      </div>
    </div>
  );
}
