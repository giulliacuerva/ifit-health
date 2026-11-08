import { createFileRoute, Link, redirect, useNavigate } from "@tanstack/react-router";
import { useState } from "react";
import { ArrowLeft, Dumbbell } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { api, getSession } from "@/lib/api";

export const Route = createFileRoute("/cadastro")({
  ssr: false,
  beforeLoad: () => {
    if (getSession()) throw redirect({ to: "/" });
  },
  head: () => ({
    meta: [
      { title: "Cadastro de aluno — iFit Health" },
      { name: "description", content: "Crie sua conta de aluno no centro esportivo iFit Health." },
      { property: "og:title", content: "Cadastro de aluno — iFit Health" },
      { property: "og:description", content: "Crie sua conta de aluno no centro esportivo iFit Health." },
    ],
  }),
  component: Register,
});

const maskCpf = (v: string) =>
  v.replace(/\D/g, "").slice(0, 11)
    .replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d)/, "$1.$2").replace(/(\d{3})(\d{1,2})$/, "$1-$2");
const maskCep = (v: string) => v.replace(/\D/g, "").slice(0, 8).replace(/(\d{5})(\d)/, "$1-$2");

function validCpf(cpf: string) {
  const d = cpf.replace(/\D/g, "");
  if (d.length !== 11 || /^(\d)\1+$/.test(d)) return false;
  for (const n of [9, 10]) {
    let s = 0;
    for (let i = 0; i < n; i++) s += Number(d[i]) * (n + 1 - i);
    if (((s * 10) % 11) % 10 !== Number(d[n])) return false;
  }
  return true;
}

function Register() {
  const navigate = useNavigate();
  const [f, setF] = useState({
    name: "", lastname: "", email: "", password: "", confirm: "", cpf: "", birthdate: "",
    gender: "", tel: "", cep: "", number: "", street: "", city: "", state: "",
  });
  const [withAddress, setWithAddress] = useState(false);
  const [errors, setErrors] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);
  const set = (k: keyof typeof f) => (e: React.ChangeEvent<HTMLInputElement>) => setF((p) => ({ ...p, [k]: e.target.value }));
  const today = new Date().toISOString().slice(0, 10);

  const validate = () => {
    const e: string[] = [];
    if (!f.name.trim()) e.push("O nome é obrigatório.");
    if (!f.lastname.trim()) e.push("O sobrenome é obrigatório.");
    if (!/^\S+@\S+\.\S+$/.test(f.email)) e.push("Informe um e-mail válido.");
    if (f.password.length < 8 || f.password.length > 72) e.push("A senha deve ter entre 8 e 72 caracteres.");
    if (f.password !== f.confirm) e.push("As senhas não conferem.");
    if (!validCpf(f.cpf)) e.push("Informe um CPF válido.");
    if (!f.birthdate) e.push("A data de nascimento é obrigatória.");
    else if (f.birthdate >= today) e.push("A data de nascimento deve estar no passado.");
    if (!f.gender) e.push("O gênero é obrigatório.");
    if (f.tel.length > 20) e.push("O telefone deve ter no máximo 20 caracteres.");
    if (withAddress) {
      if (!f.cep.trim()) e.push("O CEP é obrigatório.");
      if (!f.number.trim()) e.push("O número do endereço é obrigatório.");
      if (!f.street.trim()) e.push("A rua é obrigatória.");
      if (!f.city.trim()) e.push("A cidade é obrigatória.");
      if (!/^[A-Za-z]{2}$/.test(f.state)) e.push("Informe a sigla do estado com 2 letras.");
    }
    return e;
  };

  const submit = async (ev: React.FormEvent) => {
    ev.preventDefault();
    const e = validate(); setErrors(e);
    if (e.length) return;
    setLoading(true);
    try {
      await api.register({
        name: f.name.trim(), lastname: f.lastname.trim(), email: f.email.trim(), password: f.password,
        cpf: f.cpf, birthdate: f.birthdate, gender: f.gender as "MALE" | "FEMALE",
        tel: f.tel.trim() || undefined,
        address: withAddress ? {
          cep: f.cep, number: f.number.trim(), street: f.street.trim(), city: f.city.trim(), state: f.state.toUpperCase(),
        } : undefined,
      });
      toast.success("Conta criada com sucesso!");
      try {
        await api.login(f.email.trim(), f.password);
        navigate({ to: "/aluno/atividades" });
      } catch {
        navigate({ to: "/" });
      }
    } catch (err) {
      setErrors([err instanceof Error ? err.message : "Erro ao cadastrar."]);
    } finally { setLoading(false); }
  };

  return (
    <div className="min-h-screen">
      <header className="bg-hero text-ink-foreground">
        <div className="mx-auto flex max-w-3xl items-center justify-between px-6 py-6">
          <div className="flex items-center gap-2">
            <div className="grid size-9 place-items-center rounded-md bg-accent text-accent-foreground"><Dumbbell className="size-5" /></div>
            <span className="font-display text-xl font-bold">iFit Health</span>
          </div>
          <Link to="/" className="flex items-center gap-1 text-sm opacity-80 hover:opacity-100"><ArrowLeft className="size-4" /> Voltar ao login</Link>
        </div>
        <div className="mx-auto max-w-3xl px-6 pb-10">
          <h1 className="text-5xl font-extrabold uppercase">Crie sua <span className="text-accent">conta</span></h1>
          <p className="mt-2 opacity-80">Cadastre-se como aluno para se matricular nas atividades.</p>
        </div>
      </header>

      <form onSubmit={submit} className="mx-auto max-w-3xl space-y-8 px-6 py-10">
        <Section title="Dados pessoais">
          <F label="Nome" id="name"><Input id="name" maxLength={100} value={f.name} onChange={set("name")} /></F>
          <F label="Sobrenome" id="lastname"><Input id="lastname" maxLength={100} value={f.lastname} onChange={set("lastname")} /></F>
          <F label="CPF" id="cpf"><Input id="cpf" placeholder="000.000.000-00" value={f.cpf} onChange={(e) => setF((p) => ({ ...p, cpf: maskCpf(e.target.value) }))} /></F>
          <F label="Data de nascimento" id="birth"><Input id="birth" type="date" max={today} value={f.birthdate} onChange={set("birthdate")} /></F>
          <F label="Gênero" id="gender">
            <Select value={f.gender} onValueChange={(v) => setF((p) => ({ ...p, gender: v }))}>
              <SelectTrigger id="gender"><SelectValue placeholder="Selecione" /></SelectTrigger>
              <SelectContent>
                <SelectItem value="FEMALE">Feminino</SelectItem>
                <SelectItem value="MALE">Masculino</SelectItem>
              </SelectContent>
            </Select>
          </F>
          <F label="Telefone (opcional)" id="tel"><Input id="tel" maxLength={20} placeholder="16999990000" value={f.tel} onChange={set("tel")} /></F>
        </Section>

        <Section title="Acesso">
          <F label="E-mail" id="email" wide><Input id="email" type="email" maxLength={255} value={f.email} onChange={set("email")} /></F>
          <F label="Senha (mín. 8 caracteres)" id="pw"><Input id="pw" type="password" maxLength={72} value={f.password} onChange={set("password")} /></F>
          <F label="Confirmar senha" id="pw2"><Input id="pw2" type="password" maxLength={72} value={f.confirm} onChange={set("confirm")} /></F>
        </Section>

        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-2xl font-bold uppercase">Endereço <span className="text-base font-normal normal-case text-muted-foreground">(opcional)</span></h2>
            <label className="flex items-center gap-2 text-sm"><Switch checked={withAddress} onCheckedChange={setWithAddress} /> Informar endereço</label>
          </div>
          {withAddress && (
            <div className="grid gap-4 sm:grid-cols-2">
              <F label="CEP" id="cep"><Input id="cep" placeholder="00000-000" value={f.cep} onChange={(e) => setF((p) => ({ ...p, cep: maskCep(e.target.value) }))} /></F>
              <F label="Número" id="number"><Input id="number" maxLength={20} value={f.number} onChange={set("number")} /></F>
              <F label="Rua" id="street" wide><Input id="street" maxLength={150} value={f.street} onChange={set("street")} /></F>
              <F label="Cidade" id="city"><Input id="city" maxLength={100} value={f.city} onChange={set("city")} /></F>
              <F label="Estado (UF)" id="state"><Input id="state" maxLength={2} placeholder="SP" value={f.state} onChange={(e) => setF((p) => ({ ...p, state: e.target.value.toUpperCase() }))} /></F>
            </div>
          )}
        </div>

        {errors.length > 0 && (
          <ul className="list-disc space-y-1 rounded-lg border border-destructive/40 bg-destructive/5 p-4 pl-8 text-sm text-destructive">
            {errors.map((e) => <li key={e}>{e}</li>)}
          </ul>
        )}

        <Button type="submit" size="lg" className="w-full" disabled={loading}>{loading ? "Cadastrando..." : "Criar conta"}</Button>
      </form>
    </div>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-bold uppercase">{title}</h2>
      <div className="grid gap-4 sm:grid-cols-2">{children}</div>
    </div>
  );
}
function F({ label, id, wide, children }: { label: string; id: string; wide?: boolean; children: React.ReactNode }) {
  return <div className={`space-y-2 ${wide ? "sm:col-span-2" : ""}`}><Label htmlFor={id}>{label}</Label>{children}</div>;
}
