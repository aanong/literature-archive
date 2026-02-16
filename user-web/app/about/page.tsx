import { BookOpen, Brain, Users, Scroll } from "lucide-react";

export default function AboutPage() {
  return (
    <div className="container py-12 font-serif">
      <div className="mx-auto max-w-3xl">
        <h1 className="text-3xl font-bold tracking-tight text-primary mb-8 text-center">
          关于云章·经籍
        </h1>

        <section className="prose prose-stone max-w-none mb-12">
          <p className="text-lg text-muted-foreground leading-relaxed text-center mb-8">
            云章·经籍，汇集诸子百家、经史子集，辅以人工智能，探寻古圣先贤之智慧。
          </p>

          <div className="grid gap-8 md:grid-cols-2 mt-12">
            <div className="flex flex-col items-center text-center p-6 rounded-lg border border-border bg-card">
              <Scroll className="h-10 w-10 text-primary mb-4" />
              <h3 className="font-bold text-lg mb-2">典籍数字化</h3>
              <p className="text-sm text-muted-foreground">
                支持 TEI/Markdown 格式的古籍录入与展示，保留原文风貌
              </p>
            </div>

            <div className="flex flex-col items-center text-center p-6 rounded-lg border border-border bg-card">
              <Brain className="h-10 w-10 text-primary mb-4" />
              <h3 className="font-bold text-lg mb-2">AI 智能助读</h3>
              <p className="text-sm text-muted-foreground">
                利用大型语言模型进行古文断句、翻译、实体识别
              </p>
            </div>

            <div className="flex flex-col items-center text-center p-6 rounded-lg border border-border bg-card">
              <BookOpen className="h-10 w-10 text-primary mb-4" />
              <h3 className="font-bold text-lg mb-2">知识图谱</h3>
              <p className="text-sm text-muted-foreground">
                构建人物、地名、职官等实体关系网络，深度解读典籍
              </p>
            </div>

            <div className="flex flex-col items-center text-center p-6 rounded-lg border border-border bg-card">
              <Users className="h-10 w-10 text-primary mb-4" />
              <h3 className="font-bold text-lg mb-2">沉浸阅读</h3>
              <p className="text-sm text-muted-foreground">
                提供宋朝古风审美的阅读体验，回归经典之美
              </p>
            </div>
          </div>
        </section>

        <section className="text-center border-t border-border pt-8">
          <p className="text-sm text-muted-foreground">
            © 2026 云章书院 · 承道以文
          </p>
        </section>
      </div>
    </div>
  );
}
