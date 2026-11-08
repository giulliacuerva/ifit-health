import { createFileRoute, Link, redirect, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { Dumbbell, Server } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { api, getApiUrl, getSession, setApiUrl } from "@/lib/api";

export const Route = createFileRoute("/")({
  ssr: false,
  beforeLoad: () => {
    const s = getSession();
    if (s) throw redirect({ to: s.user.role === "ADMIN" ? "/admin/atividades" : "/aluno/atividades" });
  },
  head: () => ({
    meta: [
      { title: "Entrar — iFit Health" },
      { name: "description", content: "Acesse o centro esportivo iFit Health como aluno ou administrador." },
      { property: "og:title", content: "Entrar — iFit Health" },
      { property: "og:description", content: "Acesse o centro esportivo iFit Health como aluno ou administrador." },
    ],
  }),
  component: Login,
});

function Login() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [url, setUrl] = useState(getApiUrl());
  const [showUrl, setShowUrl] = useState(false);
  const [loading, setLoading] = useState(false);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setApiUrl(url);
    setLoading(true);
    try {
      const s = await api.login(email, password);
      toast.success(`Bem-vindo(a), ${s.user.name}!`);
      navigate({ to: s.user.role === "ADMIN" ? "/admin/atividades" : "/aluno/atividades" });
    } catch (err) {
      toast.error(err instanceof Error ? err.message : "Falha no login");
    } finally { setLoading(false); }
  };

  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <div className="bg-hero relative hidden flex-col justify-between p-12 text-ink-foreground lg:flex">
        <div className="flex items-center gap-2">
          <div className="grid size-10 place-items-center rounded-md bg-accent text-accent-foreground"><Dumbbell /></div>
          <span className="font-display text-2xl font-bold">iFit Health</span>
        </div>
        <div>
          <h1 className="text-7xl font-extrabold uppercase leading-[0.9]">
            Mova-se.<br /><span className="text-accent">Matricule-se.</span><br />Evolua.
          </h1>
          <p className="mt-6 max-w-md text-lg opacity-80">
            Natação, lutas, quadras e muito mais — escolha suas atividades e organize sua semana.
          </p>
        </div>
        <div className="text-sm opacity-60">Centro Esportivo · IFSP</div>
      </div>

      <div className="flex items-center justify-center p-6">
        <form onSubmit={submit} className="w-full max-w-sm space-y-5">
          <div>
            <h2 className="text-4xl font-bold uppercase">Entrar</h2>
            <p className="text-muted-foreground">Use seu e-mail e senha cadastrados.</p>
          </div>
          <div className="space-y-2">
            <Label htmlFor="email">E-mail</Label>
            <Input id="email" type="email" required value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
          <div className="space-y-2">
            <Label htmlFor="pw">Senha</Label>
            <Input id="pw" type="password" required value={password} onChange={(e) => setPassword(e.target.value)} />
          </div>
          <Button type="submit" size="lg" className="w-full" disabled={loading}>
            {loading ? "Entrando..." : "Entrar"}
          </Button>
          <p className="text-center text-sm text-muted-foreground">
            Ainda não tem conta? <Link to="/cadastro" className="font-semibold text-primary hover:underline">Cadastre-se</Link>
          </p>

        </form>
      </div>
    </div>
  );
}
