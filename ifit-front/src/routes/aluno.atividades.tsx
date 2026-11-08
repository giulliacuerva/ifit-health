import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { Check, Clock, MapPin, User } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { ErrorBox, PageHeader } from "@/components/AppShell";
import { api, brl, days, hhmm } from "@/lib/api";

export const Route = createFileRoute("/aluno/atividades")({
  head: () => ({ meta: [
    { title: "Atividades disponíveis — iFit Health" },
    { name: "description", content: "Escolha as atividades esportivas e faça sua matrícula." },
    { property: "og:title", content: "Atividades disponíveis — iFit Health" },
    { property: "og:description", content: "Escolha as atividades esportivas e faça sua matrícula." },
  ] }),
  component: Page,
});

function Page() {
  const qc = useQueryClient();
  const navigate = useNavigate();
  const acts = useQuery({ queryKey: ["activities", "active"], queryFn: () => api.activities(true) });
  const mine = useQuery({ queryKey: ["enrollment"], queryFn: api.myEnrollment });
  const [sel, setSel] = useState<string[]>([]);
  const [open, setOpen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const enrolledIds = new Set(mine.data?.activities.filter((a) => a.active).map((a) => a.activityClassId) ?? []);
  const selected = acts.data?.filter((a) => sel.includes(a.id)) ?? [];
  const total = selected.reduce((s, a) => s + Number(a.monthlyFee), 0);
  const toggle = (id: string) => setSel((p) => (p.includes(id) ? p.filter((x) => x !== id) : [...p, id]));

  const confirm = async () => {
    setBusy(true); setErr(null);
    try {
      await api.enroll(sel);
      await Promise.all([qc.invalidateQueries({ queryKey: ["enrollment"] }), qc.invalidateQueries({ queryKey: ["activities"] })]);
      toast.success("Matrícula realizada!");
      setOpen(false); setSel([]);
      navigate({ to: "/aluno/minhas" });
    } catch (e) { setErr(e instanceof Error ? e.message : "Erro ao matricular."); }
    finally { setBusy(false); }
  };

  return (
    <div className="pb-24">
      <PageHeader title="Atividades disponíveis" subtitle="Selecione uma ou mais atividades para se matricular" />
      {acts.error ? <ErrorBox error={acts.error} /> : acts.isLoading ? <p className="text-muted-foreground">Carregando...</p> : (
        <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          {acts.data?.length === 0 && <p className="text-muted-foreground">Nenhuma atividade disponível no momento.</p>}
          {acts.data?.map((a) => {
            const enrolled = enrolledIds.has(a.id);
            const full = a.availableSpots <= 0;
            const on = sel.includes(a.id);
            const disabled = enrolled || full;
            return (
              <Card key={a.id} onClick={() => !disabled && toggle(a.id)}
                className={`relative transition ${disabled ? "opacity-60" : "cursor-pointer hover:-translate-y-0.5 hover:shadow-lg"} ${on ? "ring-2 ring-primary" : ""}`}>
                <CardContent className="space-y-3 p-5">
                  <div className="flex items-start justify-between gap-2">
                    <h3 className="text-2xl font-bold uppercase leading-tight">{a.sportName}</h3>
                    {on && <div className="grid size-6 place-items-center rounded-full bg-primary text-primary-foreground"><Check className="size-4" /></div>}
                  </div>
                  <div className="space-y-1 text-sm text-muted-foreground">
                    <div className="flex items-center gap-2"><User className="size-4" />{a.trainerName}</div>
                    <div className="flex items-center gap-2"><MapPin className="size-4" />{a.roomName}</div>
                    <div className="flex items-center gap-2"><Clock className="size-4" />{days(a.weekdays)} · {hhmm(a.startTime)}–{hhmm(a.endTime)}</div>
                  </div>
                  <div className="flex items-center justify-between pt-2">
                    <span className="font-display text-2xl font-bold">{brl(a.monthlyFee)}<span className="text-sm font-normal text-muted-foreground">/mês</span></span>
                    {enrolled ? <Badge variant="secondary">Matriculado</Badge>
                      : full ? <Badge variant="destructive">Esgotada</Badge>
                      : <Badge variant="outline">{a.availableSpots} vaga{a.availableSpots > 1 ? "s" : ""}</Badge>}
                  </div>
                </CardContent>
              </Card>
            );
          })}
        </div>
      )}

      {sel.length > 0 && (
        <div className="fixed inset-x-0 bottom-0 z-20 border-t bg-ink text-ink-foreground md:left-64">
          <div className="flex items-center justify-between gap-4 px-6 py-4">
            <div><b>{sel.length}</b> selecionada{sel.length > 1 ? "s" : ""} · {brl(total)}/mês</div>
            <div className="flex gap-2">
              <Button variant="ghost" onClick={() => setSel([])}>Limpar</Button>
              <Button className="bg-accent text-accent-foreground hover:bg-accent/90" onClick={() => { setErr(null); setOpen(true); }}>Matricular</Button>
            </div>
          </div>
        </div>
      )}

      <Dialog open={open} onOpenChange={setOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="font-display text-2xl uppercase">Confirmar matrícula</DialogTitle>
            <DialogDescription>Revise as atividades selecionadas antes de confirmar.</DialogDescription>
          </DialogHeader>
          <div className="divide-y rounded-md border">
            {selected.map((a) => (
              <div key={a.id} className="flex items-center justify-between p-3 text-sm">
                <div><div className="font-semibold">{a.sportName}</div>
                  <div className="text-muted-foreground">{days(a.weekdays)} · {hhmm(a.startTime)}–{hhmm(a.endTime)}</div></div>
                <div className="font-medium">{brl(a.monthlyFee)}</div>
              </div>
            ))}
            <div className="flex justify-between bg-muted p-3 font-semibold"><span>Total mensal</span><span>{brl(total)}</span></div>
          </div>
          {err && <ErrorBox error={new Error(err)} />}
          <DialogFooter>
            <Button variant="outline" onClick={() => setOpen(false)}>Voltar</Button>
            <Button onClick={confirm} disabled={busy}>{busy ? "Confirmando..." : "Confirmar matrícula"}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
