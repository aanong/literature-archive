import Link from "next/link";
import { serverApi } from "@/lib/api";

interface Book {
  id: number;
  title: string;
  author: string;
  cover?: string;
  category?: string;
  summary?: string;
}

// 获取书籍列表 (Server Side)
async function getBooks(): Promise<Book[]> {
  try {
    const res = await serverApi.get("/content/books");
    return res.data || [];
  } catch (error) {
    console.error("Failed to fetch books:", error);
    return [];
  }
}

export default async function Home() {
  const books = await getBooks();

  return (
    <div className="container py-8">
      <section className="mx-auto flex max-w-[980px] flex-col items-center gap-2 py-8 md:py-12 md:pb-8 lg:py-24 lg:pb-20">
        <h1 className="text-center text-3xl font-bold leading-tight tracking-tighter md:text-6xl lg:leading-[1.1]">
          Explore Ancient Wisdom
        </h1>
        <span className="max-w-[750px] text-center text-lg text-muted-foreground sm:text-xl">
          A curated collection of classical literature, enhanced with AI-powered insights.
        </span>
      </section>

      <section className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {books.map((book) => (
          <Link
            key={book.id}
            href={`/book/${book.id}`}
            className="group relative flex flex-col overflow-hidden rounded-lg border bg-background hover:shadow-lg transition-shadow"
          >
            <div className="aspect-[3/4] overflow-hidden bg-muted">
              {book.cover ? (
                <img
                  src={book.cover}
                  alt={book.title}
                  className="h-full w-full object-cover transition-transform group-hover:scale-105"
                />
              ) : (
                <div className="flex h-full items-center justify-center bg-gray-100 text-gray-400">
                  <span className="text-4xl font-serif">📖</span>
                </div>
              )}
            </div>
            <div className="flex flex-1 flex-col space-y-2 p-4">
              <h3 className="font-semibold leading-none tracking-tight">
                {book.title}
              </h3>
              <p className="text-sm text-gray-500">{book.author}</p>
              {book.category && (
                <span className="inline-block rounded-full bg-secondary px-2 py-1 text-xs text-secondary-foreground w-fit">
                  {book.category}
                </span>
              )}
            </div>
          </Link>
        ))}
      </section>
    </div>
  );
}
