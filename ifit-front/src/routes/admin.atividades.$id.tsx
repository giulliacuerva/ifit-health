import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { ErrorBox, PageHeader } from "@/components/AppShell";
import { ActivityForm } from "@/components/ActivityForm";
import { api } from "@/lib/api";

export const Route = createFileRoute("/admin/atividades/$id")({
  head: () => ({
    meta: [
      { title: "Editar atividade — iFit Health" },
      { name: "description", content: "Altere os dados de uma atividade existente." },
      { property: "og:title", content: "Editar atividade — iFit Health" },
      { property: "og:description", content: "Altere os dados de uma atividade existente." },
    ]
  }),
  component: Page,
});

function Page() {
  const { id } = Route.useParams();
  const navigate = useNavigate();
  const qc = useQueryClient();
  const q = useQuery({ queryKey: ["activity", id], queryFn: () => api.activity(id) });
  return (
    <>
      <PageHeader title="Editar atividade" subtitle="Atualize os dados da turma" />
      {q.error ? <ErrorBox error={q.error} /> : !q.data ? <p className="text-muted-foreground">Carregando...</p> : (
        <ActivityForm initial={q.data} submitLabel="Salvar alterações" onCancel={() => navigate({ to: "/admin/atividades" })}
          onSubmit={async (data) => {
            await api.updateActivity(id, data);
            await qc.invalidateQueries({ queryKey: ["activities"] });
            await qc.invalidateQueries({ queryKey: ["activity", id] });
            toast.success("Atividade atualizada!");
            navigate({ to: "/admin/atividades" });
          }} />
      )}
    </>
  );
}
