import { createFileRoute } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { FileBarChart } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { ErrorBox, PageHeader } from "@/components/AppShell";
import { api, brl, dateBR, days, hhmm, WEEKDAYS, type Report } from "@/lib/api";

export const Route = createFileRoute("/admin/relatorios")({
  head: () => ({ meta: [
    { title: "Relatórios — iFit Health" },
    { name: "description", content: "Matriculados, receita e ocupação das atividades por período." },
    { property: "og:title", content: "Relatórios — iFit Health" },
    { property: "og:description", content: "Matriculados, receita e ocupação das atividades por período." },
  ] }),
  component: Page,
});

const ALL = "__all";

function Page() {
  const sports = useQuery({ queryKey: ["sports"], queryFn: api.sports });
  const acts = useQuery({ queryKey: ["activities", "all"], queryFn: () => api.activities() });
  const today = new Date().toISOString().slice(0, 10);
  const [start, setStart] = useState(today.slice(0, 8) + "01");
  const [end, setEnd] = useState(today);
  const [sportId, setSportId] = useState(ALL);
  const [day, setDay] = useState(ALL);
  const [report, setReport] = useState<Report | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState(false);

  const generate = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    if (end < start) { setError(new Error("A data final deve ser igual ou posterior à data inicial.")); return; }
    setLoading(true);
    try {
      setReport(await api.report({ startDate: start, endDate: end,
        sportId: sportId === ALL ? undefined : sportId, dayOfWeek: day === ALL ? undefined : day }));
    } catch (err) { setError(err); setReport(null); } finally { setLoading(false); }
  };

  const items = report?.items ?? [];
  const totalEnroll = items.reduce((s, i) => s + i.enrollmentCount, 0);
  const totalRev = items.reduce((s, i) => s + Number(i.revenue), 0);
  const avgOcc = items.length ? items.reduce((s, i) => s + Number(i.averageOccupancyPercent), 0) / items.length : 0;
  const find = (id: string) => acts.data?.find((a) => a.id === id);

  return (
    <>
      <PageHeader title="Relatórios" subtitle="Desempenho e ocupação das atividades no período" />
      <Card className="mb-6"><CardContent className="p-6">
        <form onSubmit={generate} className="grid items-end gap-4 sm:grid-cols-5">
          <div className="space-y-2"><Label>Data inicial</Label><Input type="date" required value={start} onChange={(e) => setStart(e.target.value)} /></div>
          <div className="space-y-2"><Label>Data final</Label><Input type="date" required value={end} onChange={(e) => setEnd(e.target.value)} /></div>
          <div className="space-y-2"><Label>Modalidade</Label>
            <Select value={sportId} onValueChange={setSportId}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent><SelectItem value={ALL}>Todas</SelectItem>
                {sports.data?.map((s) => <SelectItem key={s.id} value={s.id}>{s.name}</SelectItem>)}</SelectContent>
            </Select>
          </div>
          <div className="space-y-2"><Label>Dia da semana</Label>
            <Select value={day} onValueChange={setDay}>
              <SelectTrigger><SelectValue /></SelectTrigger>
              <SelectContent><SelectItem value={ALL}>Todos</SelectItem>
                {WEEKDAYS.map((d) => <SelectItem key={d.value} value={d.value}>{d.label}</SelectItem>)}</SelectContent>
            </Select>
          </div>
          <Button type="submit" size="lg" disabled={loading}><FileBarChart className="size-4" />{loading ? "Gerando..." : "Gerar relatório"}</Button>
        </form>
      </CardContent></Card>

      {error ? <ErrorBox error={error} /> : null}

      {report && (
        <>
          <p className="mb-4 text-sm text-muted-foreground">Período: {dateBR(report.startDate)} a {dateBR(report.endDate)}</p>
          <div className="mb-6 grid gap-4 sm:grid-cols-3">
            <Stat label="Matriculados" value={String(totalEnroll)} />
            <Stat label="Receita bruta acumulada" value={brl(totalRev)} />
            <Stat label="Ocupação média" value={`${avgOcc.toFixed(1)}%`} />
          </div>
          <Card className="overflow-hidden p-0">
            <Table>
              <TableHeader><TableRow>
                <TableHead>Atividade</TableHead><TableHead>Dias / Horário</TableHead>
                <TableHead className="text-right">Capacidade</TableHead><TableHead className="text-right">Matriculados</TableHead>
                <TableHead className="text-right">Receita</TableHead><TableHead className="w-48">Ocupação média</TableHead>
              </TableRow></TableHeader>
              <TableBody>
                {items.length === 0 && <TableRow><TableCell colSpan={6} className="py-10 text-center text-muted-foreground">Nenhuma atividade no período.</TableCell></TableRow>}
                {items.map((i) => {
                  const a = find(i.activityClassId); const occ = Number(i.averageOccupancyPercent);
                  return (
                    <TableRow key={i.activityClassId}>
                      <TableCell className="font-semibold">{a?.sportName ?? i.activityClassId.slice(0, 8)}
                        <div className="text-xs font-normal text-muted-foreground">{a?.trainerName}</div></TableCell>
                      <TableCell>{a ? `${days(a.weekdays)} ${hhmm(a.startTime)}–${hhmm(a.endTime)}` : "—"}</TableCell>
                      <TableCell className="text-right">{i.capacity}</TableCell>
                      <TableCell className="text-right">{i.enrollmentCount}</TableCell>
                      <TableCell className="text-right">{brl(i.revenue)}</TableCell>
                      <TableCell>
                        <div className="flex items-center gap-2">
                          <div className="h-2 flex-1 overflow-hidden rounded-full bg-muted">
                            <div className="h-full bg-primary" style={{ width: `${Math.min(occ, 100)}%` }} />
                          </div>
                          <span className="w-12 text-right text-xs">{occ.toFixed(1)}%</span>
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </Card>
        </>
      )}
    </>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  return (
    <Card><CardContent className="p-5">
      <div className="text-xs uppercase tracking-widest text-muted-foreground">{label}</div>
      <div className="font-display mt-1 text-4xl font-bold">{value}</div>
    </CardContent></Card>
  );
}
