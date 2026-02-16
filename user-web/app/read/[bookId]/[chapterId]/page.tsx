import { notFound } from "next/navigation";
import { serverApi } from "@/lib/api";
import ReactMarkdown from "react-markdown";
import { ArrowLeft, Menu } from "lucide-react";
import Link from "next/link";

interface ChapterContent {
    id: number;
    title: string;
    content: string; // Markdown content
    bookId: number;
    nextChapterId?: number;
    prevChapterId?: number;
}

// 获取章节内容
async function getChapter(chapterId: string): Promise<ChapterContent | null> {
    try {
        return await serverApi.get(`/content/chapters/${chapterId}`);
    } catch (error) {
        return null;
    }
}

export default async function ReadPage({
    params,
}: {
    params: Promise<{ bookId: string; chapterId: string }>;
}) {
    const { bookId, chapterId } = await params;
    const chapter = await getChapter(chapterId);

    if (!chapter) {
        notFound();
    }

    return (
        <div className="min-h-screen bg-[#f8f9fa] text-[#2c3e50]">
            {/* 阅读器顶部导航 */}
            <header className="fixed top-0 z-40 w-full border-b bg-white/95 backdrop-blur px-4 h-14 flex items-center justify-between shadow-sm">
                <div className="flex items-center gap-4">
                    <Link
                        href={`/book/${bookId}`}
                        className="p-2 hover:bg-gray-100 rounded-full transition-colors"
                    >
                        <ArrowLeft className="h-5 w-5" />
                    </Link>
                    <h1 className="font-serif text-lg font-medium truncate max-w-[200px] sm:max-w-md">
                        {chapter.title}
                    </h1>
                </div>
                <div className="flex items-center gap-2">
                    <button className="p-2 hover:bg-gray-100 rounded-full">
                        <Menu className="h-5 w-5" />
                    </button>
                    <button className="p-2 hover:bg-gray-100 rounded-full">
                        <span className="font-serif font-bold text-lg">A</span>
                    </button>
                </div>
            </header>

            {/* 正文区域 */}
            <main className="container max-w-3xl mx-auto pt-24 pb-24 px-6 md:px-8">
                <article className="prose prose-lg prose-stone mx-auto font-serif leading-loose text-justify">
                    <ReactMarkdown>{chapter.content || "*暂无内容*"}</ReactMarkdown>
                </article>

                {/* 底部翻页导航 */}
                <div className="mt-16 flex justify-between items-center border-t pt-8">
                    {chapter.prevChapterId ? (
                        <Link
                            href={`/read/${bookId}/${chapter.prevChapterId}`}
                            className="flex items-center gap-2 px-4 py-2 rounded-lg border hover:bg-gray-50 transition-colors"
                        >
                            <ArrowLeft className="h-4 w-4" />
                            上一章
                        </Link>
                    ) : <div />}

                    {chapter.nextChapterId ? (
                        <Link
                            href={`/read/${bookId}/${chapter.nextChapterId}`}
                            className="flex items-center gap-2 px-4 py-2 rounded-lg border hover:bg-gray-50 transition-colors"
                        >
                            下一章
                            <ArrowLeft className="h-4 w-4 rotate-180" />
                        </Link>
                    ) : <div />}
                </div>
            </main>
        </div>
    );
}
