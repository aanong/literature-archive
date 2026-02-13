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
    <div className="container py-8 font-serif">
      <section className="mx-auto flex max-w-[980px] flex-col items-center gap-4 py-8 md:py-16 md:pb-12 lg:py-32 lg:pb-24 text-center">
        <h1 className="text-4xl font-bold leading-tight tracking-widest text-primary md:text-6xl lg:leading-[1.1] mb-4">
          承道以文 · 鉴古知今
        </h1>
        <span className="max-w-[750px] text-lg text-muted-foreground sm:text-xl tracking-wider leading-relaxed">
          云章·经籍，汇集诸子百家、经史子集，<br className="hidden sm:inline" />
          辅以人工智能，探寻古圣先贤之智慧。
        </span>

        <div className="mt-8 flex flex-wrap justify-center gap-4">
          <Link
            href="/library"
            className="inline-flex h-12 items-center justify-center rounded-md bg-primary px-8 text-lg font-medium text-primary-foreground shadow transition-colors hover:bg-primary/90 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50"
          >
            探寻典籍
          </Link>
          <Link
            href="/about"
            className="inline-flex h-12 items-center justify-center rounded-md border border-input bg-background px-8 text-lg font-medium shadow-sm transition-colors hover:bg-accent hover:text-accent-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:pointer-events-none disabled:opacity-50"
          >
            关于本馆
          </Link>
        </div>
      </section>

      <div className="flex items-center justify-between px-2 mb-8 border-b border-border pb-4">
        <h2 className="text-2xl font-bold tracking-tight text-primary">藏书阁</h2>
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
              {/* 书脊线装饰 */}
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
    </div>
  );
}
