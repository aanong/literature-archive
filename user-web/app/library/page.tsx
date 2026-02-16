import Link from "next/link";
import { serverApi } from "@/lib/api";

interface Book {
  id: string;
  title: string;
  author: string;
  cover?: string;
  category?: string;
  summary?: string;
}

async function getBooks(): Promise<Book[]> {
  try {
    return await serverApi.get("/content/books") || [];
  } catch (error) {
    console.error("Failed to fetch books:", error);
    return [];
  }
}

export default async function LibraryPage() {
  const books = await getBooks();

  return (
    <div className="container py-8 font-serif">
      <div className="flex items-center justify-between px-2 mb-8 border-b border-border pb-4">
        <h1 className="text-2xl font-bold tracking-tight text-primary">藏书阁</h1>
        <p className="text-sm text-muted-foreground hidden sm:block">
          共收录 {books.length} 部典籍
        </p>
      </div>

      <section className="grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        {books.map((book) => (
          <Link
            key={book.id}
            href={`/book/${book.id}`}
            className="group relative flex flex-col overflow-hidden rounded-sm border border-border bg-card transition-all hover:shadow-lg hover:shadow-accent/20 hover:-translate-y-1"
          >
            <div className="aspect-[3/4] overflow-hidden bg-muted relative">
              {book.cover ? (
                <img
                  src={book.cover}
                  alt={book.title}
                  className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
                />
              ) : (
                <div className="flex h-full flex-col items-center justify-center bg-[#f0ebe5] text-muted-foreground/50">
                  <span className="text-6xl mb-2 opacity-50">📖</span>
                  <span className="text-sm font-serif tracking-widest text-primary/40">云章藏书</span>
                </div>
              )}
              <div className="absolute left-3 top-0 bottom-0 w-[1px] bg-black/10 z-10"></div>
              <div className="absolute left-0 top-0 bottom-0 w-8 bg-black/5 z-0"></div>
            </div>

            <div className="flex flex-1 flex-col space-y-3 p-5 bg-card relative">
              <div className="space-y-1">
                <h3 className="font-bold text-lg leading-tight tracking-wide text-primary group-hover:text-accent transition-colors">
                  {book.title}
                </h3>
                <p className="text-sm text-muted-foreground italic font-serif">
                  {book.author ? `[${book.author}]` : '佚名'} 著
                </p>
              </div>

              {book.category && (
                <div className="pt-2">
                  <span className="inline-block rounded-sm bg-secondary/10 px-2 py-0.5 text-xs font-medium text-secondary border border-secondary/20">
                    {book.category}
                  </span>
                </div>
              )}

              {book.summary && (
                <p className="line-clamp-2 text-xs text-muted-foreground/80 leading-relaxed mt-2 border-t border-dashed border-border/50 pt-2">
                  {book.summary}
                </p>
              )}
            </div>
          </Link>
        ))}
      </section>

      {books.length === 0 && (
        <div className="text-center py-16 text-muted-foreground">
          <p className="text-lg">暂无典籍</p>
          <p className="text-sm mt-2">请稍后再来探寻</p>
        </div>
      )}
    </div>
  );
}
