import Link from "next/link";
import { notFound } from "next/navigation";
import { serverApi } from "@/lib/api";
import { ArrowLeft, BookOpen, Clock } from "lucide-react";

interface Chapter {
    id: number;
    title: string;
    wordCount: number;
}

interface Volume {
    id: number;
    title: string;
    chapters: Chapter[];
}

interface BookDetail {
    id: number;
    title: string;
    author: string;
    cover?: string;
    summary?: string;
    category?: string;
    volumes: Volume[];
}

async function getBook(id: string): Promise<BookDetail | null> {
    try {
        const res = await serverApi.get(`/content/books/${id}`);
        return res.data;
    } catch (error) {
        return null;
    }
}

export default async function BookPage({
    params,
}: {
    params: { id: string };
}) {
    const book = await getBook(params.id);

    if (!book) {
        notFound();
    }

    return (
        <div className="container py-8 md:py-12">
            <Link
                href="/"
                className="mb-8 inline-flex items-center text-sm text-muted-foreground hover:text-foreground"
            >
                <ArrowLeft className="mr-2 h-4 w-4" />
                Back to Library
            </Link>

            <div className="grid gap-8 md:grid-cols-[300px_1fr]">
                <div className="space-y-4">
                    <div className="aspect-[3/4] overflow-hidden rounded-lg border bg-muted">
                        {book.cover ? (
                            <img
                                src={book.cover}
                                alt={book.title}
                                className="h-full w-full object-cover"
                            />
                        ) : (
                            <div className="flex h-full items-center justify-center bg-gray-100 text-gray-400">
                                <span className="text-6xl font-serif">📖</span>
                            </div>
                        )}
                    </div>
                    <div className="space-y-2">
                        <h1 className="text-3xl font-bold">{book.title}</h1>
                        <p className="text-lg text-muted-foreground">{book.author}</p>
                        {book.category && (
                            <div className="inline-block rounded-full bg-secondary px-3 py-1 text-sm text-secondary-foreground">
                                {book.category}
                            </div>
                        )}
                    </div>
                </div>

                <div className="space-y-8">
                    <div className="prose max-w-none">
                        <h2 className="text-xl font-semibold">Summary</h2>
                        <p className="text-muted-foreground leading-relaxed">
                            {book.summary || "No summary available."}
                        </p>
                    </div>

                    <div className="space-y-4">
                        <h2 className="text-xl font-semibold">Table of Contents</h2>
                        <div className="rounded-lg border">
                            {book.volumes && book.volumes.length > 0 ? (
                                book.volumes.map((volume) => (
                                    <div key={volume.id} className="border-b last:border-0">
                                        <div className="bg-muted/50 px-4 py-3 font-medium">
                                            {volume.title}
                                        </div>
                                        <div className="divide-y p-0">
                                            {volume.chapters.map((chapter) => (
                                                <Link
                                                    key={chapter.id}
                                                    href={`/read/${book.id}/${chapter.id}`}
                                                    className="flex items-center justify-between px-4 py-3 hover:bg-accent hover:text-accent-foreground transition-colors"
                                                >
                                                    <div className="flex items-center gap-3">
                                                        <BookOpen className="h-4 w-4 text-muted-foreground" />
                                                        <span>{chapter.title}</span>
                                                    </div>
                                                    {chapter.wordCount && (
                                                        <div className="flex items-center gap-1 text-xs text-muted-foreground">
                                                            <Clock className="h-3 w-3" />
                                                            <span>{Math.ceil(chapter.wordCount / 500)} min</span>
                                                        </div>
                                                    )}
                                                </Link>
                                            ))}
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <div className="p-4 text-muted-foreground">No chapters found.</div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
