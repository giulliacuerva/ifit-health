import { createFileRoute, Outlet, redirect } from "@tanstack/react-router";
import { BarChart3, CalendarRange } from "lucide-react";
import { AppShell } from "@/components/AppShell";
import { getSession } from "@/lib/api";

export const Route = createFileRoute("/admin")({
  ssr: false,
  beforeLoad: () => {
    const s = getSession();
    if (!s) throw redirect({ to: "/" });
    if (s.user.role !== "ADMIN") throw redirect({ to: "/aluno/atividades" });
  },
  component: () => (
    <AppShell area="Administração" nav={[
      { to: "/admin/atividades", label: "Atividades", icon: <CalendarRange className="size-4" /> },
      { to: "/admin/relatorios", label: "Relatórios", icon: <BarChart3 className="size-4" /> },
    ]}>
      <Outlet />
    </AppShell>
  ),
});
