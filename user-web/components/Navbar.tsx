import Link from "next/link";
import { BookOpen, Search, User } from "lucide-react";

export function Navbar() {
    return (
        <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
            <div className="container flex h-14 items-center">
                <Link href="/" className="mr-6 flex items-center space-x-2">
                    <BookOpen className="h-6 w-6" />
                    <span className="hidden font-bold sm:inline-block">
                        Literature Archive
                    </span>
                </Link>
                <nav className="flex items-center space-x-6 text-sm font-medium">
                    <Link
                        href="/"
                        className="transition-colors hover:text-foreground/80 text-foreground/60"
                    >
                        Library
                    </Link>
                    <Link
                        href="/about"
                        className="transition-colors hover:text-foreground/80 text-foreground/60"
                    >
                        About
                    </Link>
                </nav>
                <div className="ml-auto flex items-center space-x-4">
                    <button className="inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors hover:bg-accent hover:text-accent-foreground h-9 w-9">
                        <Search className="h-5 w-5" />
                        <span className="sr-only">Search</span>
                    </button>
                    <button className="inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors hover:bg-accent hover:text-accent-foreground h-9 w-9">
                        <User className="h-5 w-5" />
                        <span className="sr-only">User</span>
                    </button>
                </div>
            </div>
        </header>
    );
}
