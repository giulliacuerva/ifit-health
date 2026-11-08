import { createFileRoute, Outlet, redirect } from "@tanstack/react-router";
import { ClipboardList, LayoutGrid } from "lucide-react";
import { AppShell } from "@/components/AppShell";
import { getSession } from "@/lib/api";

export const Route = createFileRoute("/aluno")({
  ssr: false,
  beforeLoad: () => {
    const s = getSession();
    if (!s) throw redirect({ to: "/" });
    if (s.user.role !== "USER") throw redirect({ to: "/admin/atividades" });
  },
  component: () => (
    <AppShell area="Área do aluno" nav={[
      { to: "/aluno/atividades", label: "Atividades disponíveis", icon: <LayoutGrid className="size-4" /> },
      { to: "/aluno/minhas", label: "Minhas atividades", icon: <ClipboardList className="size-4" /> },
    ]}>
      <Outlet />
    </AppShell>
  ),
});
