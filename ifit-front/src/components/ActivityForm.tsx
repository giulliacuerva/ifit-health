import { useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { AlertTriangle } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { TimeInput } from "@/components/ui/time-input";
import { Card, CardContent } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { ErrorBox } from "@/components/AppShell";
import {
    api, ApiError, days, hhmm, ROOM_TYPES, WEEKDAYS,
    type ActivityRequest, type AffectedStudent, type Weekday,
} from "@/lib/api";

export function ActivityForm({ initial, onSubmit, submitLabel, onCancel }: {
    initial?: Partial<ActivityRequest>; submitLabel: string; onCancel: () => void;
    onSubmit: (data: ActivityRequest) => Promise<void>;
}) {
    const sports = useQuery({ queryKey: ["sports"], queryFn: api.sports });
    const rooms = useQuery({ queryKey: ["rooms"], queryFn: api.rooms });
    const trainers = useQuery({ queryKey: ["trainers"], queryFn: api.trainers });

    const [f, setF] = useState({
        sportId: initial?.sportId ?? "", roomId: initial?.roomId ?? "", trainerId: initial?.trainerId ?? "",
        weekdays: (initial?.weekdays ?? []) as Weekday[],
        startTime: hhmm(initial?.startTime ?? ""), endTime: hhmm(initial?.endTime ?? ""),
        capacity: String(initial?.capacity ?? ""), monthlyFee: String(initial?.monthlyFee ?? ""),
    });
    const [errors, setErrors] = useState<string[]>([]);
    const [apiErr, setApiErr] = useState<string | null>(null);
    const [affected, setAffected] = useState<AffectedStudent[]>([]);
    const [saving, setSaving] = useState(false);

    const set = (k: keyof typeof f, v: any) => setF((p) => ({ ...p, [k]: v }));
    const sport = sports.data?.find((s) => s.id === f.sportId);
    const room = rooms.data?.find((r) => r.id === f.roomId);

    const validate = () => {
        const e: string[] = [];
        if (!f.sportId) e.push("Selecione a modalidade.");
        if (!f.roomId) e.push("Selecione a sala.");
        if (!f.trainerId) e.push("Selecione o professor.");
        if (f.weekdays.length === 0) e.push("Selecione ao menos um dia da semana.");
        if (!f.startTime || !f.endTime) e.push("Informe horário de início e fim.");
        else if (f.endTime <= f.startTime) e.push("O horário de término deve ser após o início.");
        const cap = Number(f.capacity);
        if (!Number.isInteger(cap) || cap <= 0) e.push("Capacidade deve ser um número inteiro positivo.");
        else if (room && cap > room.capacity) e.push(`Capacidade excede o limite da sala (${room.capacity}).`);
        if (f.monthlyFee === "" || Number(f.monthlyFee) < 0) e.push("Mensalidade deve ser maior ou igual a zero.");
        if (sport && room && sport.roomType !== room.roomType)
            e.push(`A modalidade ${sport.name} exige sala do tipo ${ROOM_TYPES[sport.roomType] ?? sport.roomType}.`);
        return e;
    };

    const submit = async (ev: React.FormEvent) => {
        ev.preventDefault();
        setApiErr(null); setAffected([]);
        const e = validate(); setErrors(e);
        if (e.length) return;
        setSaving(true);
        try {
            await onSubmit({
                sportId: f.sportId, roomId: f.roomId, trainerId: f.trainerId, weekdays: f.weekdays,
                startTime: f.startTime, endTime: f.endTime, capacity: Number(f.capacity), monthlyFee: Number(f.monthlyFee),
            });
        } catch (err) {
            setApiErr(err instanceof Error ? err.message : "Erro ao salvar.");
            if (err instanceof ApiError && Array.isArray(err.body?.affectedStudents)) setAffected(err.body.affectedStudents);
        } finally { setSaving(false); }
    };

    if (sports.error || rooms.error || trainers.error) return <ErrorBox error={sports.error || rooms.error || trainers.error} />;

    return (
        <form onSubmit={submit} className="max-w-3xl space-y-6">
            <Card><CardContent className="grid gap-5 p-6 sm:grid-cols-3">
                <Field label="Modalidade">
                    <Select value={f.sportId} onValueChange={(v) => set("sportId", v)}>
                        <SelectTrigger><SelectValue placeholder="Selecione" /></SelectTrigger>
                        <SelectContent>{sports.data?.filter((s) => s.active || s.id === f.sportId).map((s) => (
                            <SelectItem key={s.id} value={s.id}>{s.name}</SelectItem>))}</SelectContent>
                    </Select>
                </Field>
                <Field label="Sala">
                    <Select value={f.roomId} onValueChange={(v) => set("roomId", v)}>
                        <SelectTrigger><SelectValue placeholder="Selecione" /></SelectTrigger>
                        <SelectContent>{rooms.data?.map((r) => (
                            <SelectItem key={r.id} value={r.id}>{r.name} · {ROOM_TYPES[r.roomType] ?? r.roomType} ({r.capacity})</SelectItem>))}</SelectContent>
                    </Select>
                </Field>
                <Field label="Professor">
                    <Select value={f.trainerId} onValueChange={(v) => set("trainerId", v)}>
                        <SelectTrigger><SelectValue placeholder="Selecione" /></SelectTrigger>
                        <SelectContent>{trainers.data?.filter((t) => t.active || t.id === f.trainerId).map((t) => (
                            <SelectItem key={t.id} value={t.id}>{t.name}</SelectItem>))}</SelectContent>
                    </Select>
                </Field>
            </CardContent></Card>

            <Card><CardContent className="space-y-5 p-6">
                <Field label="Dias da semana">
                    <div className="flex flex-wrap gap-2">
                        {WEEKDAYS.map((d) => {
                            const on = f.weekdays.includes(d.value);
                            return (
                                <Button key={d.value} type="button" size="sm" variant={on ? "default" : "outline"}
                                    onClick={() => set("weekdays", on ? f.weekdays.filter((w) => w !== d.value) : [...f.weekdays, d.value])}>
                                    {d.label}
                                </Button>
                            );
                        })}
                    </div>
                </Field>
                <div className="grid gap-5 sm:grid-cols-4">
                    <Field label="Início"><TimeInput value={f.startTime} onChange={(v) => set("startTime", v)} /></Field>
                    <Field label="Término"><TimeInput value={f.endTime} onChange={(v) => set("endTime", v)} /></Field>
                    <Field label="Capacidade"><Input type="number" min={1} value={f.capacity} onChange={(e) => set("capacity", e.target.value)} /></Field>
                    <Field label="Mensalidade (R$)"><Input type="number" min={0} step="0.01" value={f.monthlyFee} onChange={(e) => set("monthlyFee", e.target.value)} /></Field>
                </div>
            </CardContent></Card>

            {errors.length > 0 && (
                <ul className="list-disc space-y-1 rounded-lg border border-destructive/40 bg-destructive/5 p-4 pl-8 text-sm text-destructive">
                    {errors.map((e) => <li key={e}>{e}</li>)}
                </ul>
            )}
            {apiErr && (
                <div className="rounded-lg border border-destructive/40 bg-destructive/5 p-4 text-sm text-destructive">
                    <div className="flex items-center gap-2 font-semibold"><AlertTriangle className="size-4" />{apiErr}</div>
                    {affected.length > 0 && (
                        <div className="mt-3 space-y-2 text-foreground">
                            <div className="font-medium">Alunos impactados:</div>
                            {affected.map((s) => (
                                <div key={s.customerId} className="rounded-md bg-card p-3">
                                    <div className="font-semibold">{s.customerName}</div>
                                    {s.conflictingActivities.map((c) => (
                                        <div key={c.activityClassId} className="text-muted-foreground">
                                            {c.sportName} — {days(c.weekdays)} {hhmm(c.startTime)}–{hhmm(c.endTime)}
                                        </div>
                                    ))}
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}

            <div className="flex gap-3">
                <Button type="submit" size="lg" disabled={saving}>{saving ? "Salvando..." : submitLabel}</Button>
                <Button type="button" size="lg" variant="outline" onClick={onCancel}>Cancelar</Button>
            </div>
        </form>
    );
}

function Field({ label, children }: { label: string; children: React.ReactNode }) {
    return <div className="space-y-2"><Label>{label}</Label>{children}</div>;
}
