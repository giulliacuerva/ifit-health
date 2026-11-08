import { Link, useNavigate } from "@tanstack/react-router";
import { useQueryClient } from "@tanstack/react-query";
import { LogOut, Dumbbell } from "lucide-react";
import type { ReactNode } from "react";
import { Button } from "@/components/ui/button";
import { clearSession, getSession } from "@/lib/api";

export function AppShell({ nav, children, area }: {
    nav: { to: string; label: string; icon: ReactNode }[]; children: ReactNode; area: string;
}) {
    const user = getSession()?.user;
    const navigate = useNavigate();
    const qc = useQueryClient();
    const logout = async () => {
        await qc.cancelQueries(); qc.clear(); clearSession();
        navigate({ to: "/", replace: true });
    };
    return (
        <div className="min-h-screen md:flex">
            <aside className="bg-sidebar text-sidebar-foreground md:w-64 md:min-h-screen flex md:flex-col">
                <div className="flex items-center gap-2 px-5 py-5">
                    <div className="grid size-9 place-items-center rounded-md bg-sidebar-primary text-sidebar-primary-foreground">
                        <Dumbbell className="size-5" />
                    </div>
                    <div>
                        <div className="font-display text-xl font-bold leading-none">iFit Health</div>
                        <div className="text-xs uppercase tracking-widest opacity-60">{area}</div>
                    </div>
                </div>
                <nav className="flex flex-1 gap-1 px-3 md:flex-col md:py-4">
                    {nav.map((n) => (
                        <Link key={n.to} to={n.to}
                            className="flex items-center gap-3 rounded-md px-3 py-2.5 text-sm font-medium opacity-80 transition hover:bg-sidebar-accent hover:opacity-100"
                            activeProps={{ className: "bg-sidebar-accent !opacity-100 text-sidebar-primary" }}>
                            {n.icon}<span className="hidden sm:inline">{n.label}</span>
                        </Link>
                    ))}
                </nav>
                <div className="hidden border-t border-sidebar-border p-4 md:block">
                    <div className="text-sm font-semibold">{user?.name} {user?.lastname}</div>
                    <div className="mb-3 truncate text-xs opacity-60">{user?.email}</div>
                    <Button variant="secondary" size="sm" className="w-full" onClick={logout}>
                        <LogOut className="size-4" /> Sair
                    </Button>
                </div>
                <Button variant="ghost" size="icon" className="m-3 md:hidden" onClick={logout} aria-label="Sair">
                    <LogOut className="size-4" />
                </Button>
            </aside>
            <main className="flex-1 p-5 md:p-10">{children}</main>
        </div>
    );
}

export function PageHeader({ title, subtitle, action }: { title: string; subtitle?: string; action?: ReactNode }) {
    return (
        <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
            <div>
                <h1 className="text-4xl font-bold uppercase">{title}</h1>
                {subtitle && <p className="mt-1 text-muted-foreground">{subtitle}</p>}
            </div>
            {action}
        </div>
    );
}

export function ErrorBox({ error }: { error: unknown }) {
    return (
        <div className="rounded-lg border border-destructive/40 bg-destructive/5 p-4 text-sm text-destructive">
            {error instanceof Error ? error.message : "Erro inesperado."}
        </div>
    );
}
