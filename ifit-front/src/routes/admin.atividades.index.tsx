import { createFileRoute, Link } from "@tanstack/react-router";
import { useQuery } from "@tanstack/react-query";
import { Pencil, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Card } from "@/components/ui/card";
import { ErrorBox, PageHeader } from "@/components/AppShell";
import { api, brl, days, hhmm } from "@/lib/api";

export const Route = createFileRoute("/admin/atividades/")({
  head: () => ({ meta: [
    { title: "Gerenciar atividades — iFit Health" },
    { name: "description", content: "Cadastre e edite a grade de atividades do centro esportivo." },
    { property: "og:title", content: "Gerenciar atividades — iFit Health" },
    { property: "og:description", content: "Cadastre e edite a grade de atividades do centro esportivo." },
  ] }),
  component: Page,
});

function Page() {
  const q = useQuery({ queryKey: ["activities", "all"], queryFn: () => api.activities() });
  return (
    <>
      <PageHeader title="Atividades" subtitle="Grade de atividades cadastradas"
        action={<Button asChild size="lg"><Link to="/admin/atividades/nova"><Plus className="size-4" /> Nova atividade</Link></Button>} />
      {q.error ? <ErrorBox error={q.error} /> : (
        <Card className="overflow-hidden p-0">
          <Table>
            <TableHeader><TableRow>
              <TableHead>Modalidade</TableHead><TableHead>Professor</TableHead><TableHead>Sala</TableHead>
              <TableHead>Dias / Horário</TableHead><TableHead className="text-right">Vagas</TableHead>
              <TableHead className="text-right">Mensalidade</TableHead><TableHead>Status</TableHead><TableHead />
            </TableRow></TableHeader>
            <TableBody>
              {q.isLoading && <TableRow><TableCell colSpan={8} className="text-center text-muted-foreground">Carregando...</TableCell></TableRow>}
              {q.data?.length === 0 && <TableRow><TableCell colSpan={8} className="py-10 text-center text-muted-foreground">Nenhuma atividade cadastrada.</TableCell></TableRow>}
              {q.data?.map((a) => (
                <TableRow key={a.id}>
                  <TableCell className="font-semibold">{a.sportName}</TableCell>
                  <TableCell>{a.trainerName}</TableCell>
                  <TableCell>{a.roomName}</TableCell>
                  <TableCell>{days(a.weekdays)}<div className="text-xs text-muted-foreground">{hhmm(a.startTime)}–{hhmm(a.endTime)}</div></TableCell>
                  <TableCell className="text-right">{a.enrolledCount}/{a.capacity}</TableCell>
                  <TableCell className="text-right">{brl(a.monthlyFee)}</TableCell>
                  <TableCell><Badge variant={a.active ? "default" : "secondary"}>{a.active ? "Ativa" : "Inativa"}</Badge></TableCell>
                  <TableCell>
                    <Button asChild variant="ghost" size="sm">
                      <Link to="/admin/atividades/$id" params={{ id: a.id }}><Pencil className="size-4" /> Editar</Link>
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Card>
      )}
    </>
  );
}
