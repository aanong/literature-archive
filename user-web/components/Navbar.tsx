import Link from "next/link";
import { BookOpen, Search, User } from "lucide-react";

export function Navbar() {
    return (
        <header className="sticky top-0 z-50 w-full border-b border-border/40 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60 font-serif">
            <div className="container flex h-16 items-center">
                <Link href="/" className="mr-8 flex items-center space-x-3 transition-colors hover:opacity-80">
                    <BookOpen className="h-6 w-6 text-primary-foreground" />
                    <span className="hidden text-lg font-bold tracking-widest text-primary sm:inline-block">
                        云章·经籍
                    </span>
                </Link>
                <nav className="flex items-center space-x-6 text-sm font-medium">
                    <Link
                        href="/"
                        className="transition-colors hover:text-primary text-muted-foreground hover:font-bold"
                    >
                        藏书阁
                    </Link>
                    <Link
                        href="/about"
                        className="transition-colors hover:text-primary text-muted-foreground hover:font-bold"
                    >
                        关于
                    </Link>
                </nav>
                <div className="ml-auto flex items-center space-x-4">
                    <button className="inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors hover:bg-accent/20 hover:text-accent-foreground h-9 w-9">
                        <Search className="h-5 w-5 text-muted-foreground hover:text-primary" />
                        <span className="sr-only">搜索</span>
                    </button>
                    <button className="inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors hover:bg-accent/20 hover:text-accent-foreground h-9 w-9">
                        <User className="h-5 w-5 text-muted-foreground hover:text-primary" />
                        <span className="sr-only">用户</span>
                    </button>
                </div>
            </div>
        </header>
    );
}
