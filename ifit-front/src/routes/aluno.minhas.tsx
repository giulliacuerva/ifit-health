import { createFileRoute } from "@tanstack/react-router";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { ErrorBox, PageHeader } from "@/components/AppShell";
import { api, brl, dateBR, days, hhmm, type EnrollmentActivity } from "@/lib/api";

export const Route = createFileRoute("/aluno/minhas")({
  head: () => ({
    meta: [
      { title: "Minhas atividades — iFit Health" },
      { name: "description", content: "Suas matrículas ativas e histórico de atividades." },
      { property: "og:title", content: "Minhas atividades — iFit Health" },
      { property: "og:description", content: "Suas matrículas ativas e histórico de atividades." },
    ]
  }),
  component: Page,
});

function Page() {
  const qc = useQueryClient();
  const mine = useQuery({ queryKey: ["enrollment"], queryFn: api.myEnrollment });
  const acts = useQuery({ queryKey: ["activities", "all"], queryFn: () => api.activities() });
  const [target, setTarget] = useState<EnrollmentActivity | null>(null);
  const today = new Date().toISOString().slice(0, 10);
  const [endDate, setEndDate] = useState(today);
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  const find = (id: string) => acts.data?.find((a) => a.id === id);
  const list = mine.data?.activities ?? [];
  const active = list.filter((a) => a.active);
  const history = list.filter((a) => !a.active);

  const cancel = async () => {
    if (!target) return;
    setErr(null);
    if (!endDate) return setErr("Informe a data de encerramento.");
    if (endDate < target.startDate) return setErr("A data de encerramento não pode ser anterior à data de início.");
    setBusy(true);
    try {
      await api.cancel(target.enrollmentActivityId, endDate);
      await Promise.all([qc.invalidateQueries({ queryKey: ["enrollment"] }), qc.invalidateQueries({ queryKey: ["activities"] })]);
      toast.success("Matrícula encerrada.");
      setTarget(null);
    } catch (e) { setErr(e instanceof Error ? e.message : "Erro ao cancelar."); }
    finally { setBusy(false); }
  };

  const Item = ({ e }: { e: EnrollmentActivity }) => {
    const a = find(e.activityClassId);
    return (
      <Card><CardContent className="flex flex-wrap items-center justify-between gap-4 p-5">
        <div>
          <h3 className="text-2xl font-bold uppercase">{a?.sportName ?? "Atividade"}</h3>
          <div className="text-sm text-muted-foreground">
            {a?.trainerName} · {a ? `${days(a.weekdays)} ${hhmm(a.startTime)}–${hhmm(a.endTime)}` : ""}
          </div>
          <div className="mt-1 text-sm">Início: <b>{dateBR(e.startDate)}</b>{!e.active && <> · Encerrada em: <b>{dateBR(e.endDate)}</b></>}</div>
        </div>
        <div className="flex items-center gap-4">
          <span className="font-display text-2xl font-bold">{brl(e.monthlyFee)}<span className="text-sm font-normal text-muted-foreground">/mês</span></span>
          {e.active && <Button variant="outline" className="border-destructive/50 text-destructive hover:bg-destructive/10"
            onClick={() => { setErr(null); setEndDate(today); setTarget(e); }}>Cancelar</Button>}
        </div>
      </CardContent></Card>
    );
  };

  const err0 = mine.error || acts.error;
  return (
    <>
      <PageHeader title="Minhas atividades" subtitle="Acompanhe suas matrículas" />
      {err0 ? <ErrorBox error={err0} /> : mine.isLoading ? <p className="text-muted-foreground">Carregando...</p> : (
        <Tabs defaultValue="ativas">
          <TabsList><TabsTrigger value="ativas">Ativas ({active.length})</TabsTrigger><TabsTrigger value="historico">Histórico ({history.length})</TabsTrigger></TabsList>
          <TabsContent value="ativas" className="mt-4 space-y-3">
            {active.length === 0 ? <p className="text-muted-foreground">Você não possui matrículas ativas.</p> : active.map((e) => <Item key={e.enrollmentActivityId} e={e} />)}
          </TabsContent>
          <TabsContent value="historico" className="mt-4 space-y-3">
            {history.length === 0 ? <p className="text-muted-foreground">Nenhuma atividade encerrada.</p> : history.map((e) => <Item key={e.enrollmentActivityId} e={e} />)}
          </TabsContent>
        </Tabs>
      )}

      <Dialog open={!!target} onOpenChange={(o) => !o && setTarget(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle className="font-display text-2xl uppercase">Cancelar matrícula</DialogTitle>
            <DialogDescription>
              Apenas a atividade {target ? find(target.activityClassId)?.sportName : ""} será encerrada. Ela ficará no seu histórico.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-2">
            <Label htmlFor="end">Data de encerramento</Label>
            <Input id="end" type="date" value={endDate} min={target?.startDate} onChange={(e) => setEndDate(e.target.value)} />
          </div>
          {err && <ErrorBox error={new Error(err)} />}
          <DialogFooter>
            <Button variant="outline" onClick={() => setTarget(null)}>Voltar</Button>
            <Button variant="destructive" onClick={cancel} disabled={busy}>{busy ? "Encerrando..." : "Confirmar cancelamento"}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
