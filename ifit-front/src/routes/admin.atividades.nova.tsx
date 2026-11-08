import { createFileRoute, useNavigate } from "@tanstack/react-router";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { PageHeader } from "@/components/AppShell";
import { ActivityForm } from "@/components/ActivityForm";
import { api } from "@/lib/api";

export const Route = createFileRoute("/admin/atividades/nova")({
  head: () => ({ meta: [
    { title: "Nova atividade — iFit Health" },
    { name: "description", content: "Cadastre uma nova atividade na grade." },
    { property: "og:title", content: "Nova atividade — iFit Health" },
    { property: "og:description", content: "Cadastre uma nova atividade na grade." },
  ] }),
  component: Page,
});

function Page() {
  const navigate = useNavigate();
  const qc = useQueryClient();
  return (
    <>
      <PageHeader title="Nova atividade" subtitle="Preencha os dados da turma" />
      <ActivityForm submitLabel="Cadastrar atividade" onCancel={() => navigate({ to: "/admin/atividades" })}
        onSubmit={async (data) => {
          await api.createActivity(data);
          await qc.invalidateQueries({ queryKey: ["activities"] });
          toast.success("Atividade cadastrada!");
          navigate({ to: "/admin/atividades" });
        }} />
    </>
  );
}
