// Client for the iFit Health Spring Boot backend (JWT bearer auth).
export type Role = "USER" | "ADMIN";
export type Weekday =
    | "MONDAY" | "TUESDAY" | "WEDNESDAY" | "THURSDAY" | "FRIDAY" | "SATURDAY" | "SUNDAY";

export const WEEKDAYS: { value: Weekday; label: string; short: string }[] = [
    { value: "MONDAY", label: "Segunda", short: "Seg" },
    { value: "TUESDAY", label: "Terça", short: "Ter" },
    { value: "WEDNESDAY", label: "Quarta", short: "Qua" },
    { value: "THURSDAY", label: "Quinta", short: "Qui" },
    { value: "FRIDAY", label: "Sexta", short: "Sex" },
    { value: "SATURDAY", label: "Sábado", short: "Sáb" },
    { value: "SUNDAY", label: "Domingo", short: "Dom" },
];

export const ROOM_TYPES: Record<string, string> = {
    POOL: "Piscina", COURT: "Quadra", TATAMI: "Tatame",
    TABLE_TENNIS: "Tênis de mesa", GYM: "Academia", TRACK: "Pista",
};

export interface AuthUser {
    userId: string; customerId: string | null; name: string; lastname: string; email: string; role: Role;
}
export interface RegisterRequest {
    name: string; lastname: string; email: string; password: string; cpf: string;
    birthdate: string; gender: "MALE" | "FEMALE"; tel?: string | undefined;
    address?: { cep: string; number: string; street: string; city: string; state: string } | undefined;
}
export interface Activity {
    id: string; sportId: string; roomId: string; trainerId: string; weekdays: Weekday[];
    startTime: string; endTime: string; capacity: number; monthlyFee: number; active: boolean;
    sportName: string; roomName: string; trainerName: string; enrolledCount: number; availableSpots: number;
}
export interface ActivityRequest {
    sportId: string; roomId: string; trainerId: string; weekdays: Weekday[];
    startTime: string; endTime: string; capacity: number; monthlyFee: number;
}
export interface Sport { id: string; name: string; description: string; roomType: string; active: boolean }
export interface Room { id: string; name: string; roomType: string; capacity: number }
export interface Trainer { id: string; name: string; active: boolean }
export interface EnrollmentActivity {
    enrollmentActivityId: string; activityClassId: string; monthlyFee: number;
    active: boolean; startDate: string; endDate: string | null;
}
export interface Enrollment { id: string; customerId: string; active: boolean; activities: EnrollmentActivity[] }
export interface ReportItem {
    activityClassId: string; capacity: number; enrollmentCount: number; revenue: number; averageOccupancyPercent: number;
}
export interface Report { startDate: string; endDate: string; items: ReportItem[] }
export interface AffectedStudent {
    customerId: string; customerName: string;
    conflictingActivities: { activityClassId: string; sportName: string; weekdays: Weekday[]; startTime: string; endTime: string }[];
}

const KEY_URL = "ifit.apiUrl";
const KEY_SESSION = "ifit.session";
export const DEFAULT_API_URL = "http://localhost:8080";

export function getApiUrl() {
    if (typeof window === "undefined") return DEFAULT_API_URL;
    return localStorage.getItem(KEY_URL) || DEFAULT_API_URL;
}
export function setApiUrl(url: string) {
    localStorage.setItem(KEY_URL, url.replace(/\/+$/, ""));
}

export interface Session { token: string; user: AuthUser }
export function getSession(): Session | null {
    if (typeof window === "undefined") return null;
    try { return JSON.parse(localStorage.getItem(KEY_SESSION) || "null"); } catch { return null; }
}
export function clearSession() { localStorage.removeItem(KEY_SESSION); }

export class ApiError extends Error {
    constructor(message: string, public status: number, public body?: any) { super(message); }
}

async function request<T>(path: string, init: RequestInit = {}, token = getSession()?.token): Promise<T> {
    let res: Response;
    try {
        res = await fetch(getApiUrl() + path, {
            ...init,
            headers: {
                "Content-Type": "application/json",
                ...(token ? { Authorization: `Bearer ${token}` } : {}),
                ...init.headers,
            },
        });
    } catch {
        throw new ApiError(`Não foi possível conectar ao servidor (${getApiUrl()}). Verifique se o backend está rodando e com CORS liberado.`, 0);
    }
    const text = await res.text();
    let body: any = undefined;
    try { body = text ? JSON.parse(text) : undefined; } catch { body = text; }
    if (!res.ok) {
        if (res.status === 401 && typeof window !== "undefined" && path !== "/api/v1/authenticate") {
            clearSession();
        }
        const fallback: Record<number, string> = {
            400: "Dados inválidos.", 401: "Sessão expirada ou credenciais inválidas.",
            403: "Você não tem permissão para esta operação.", 404: "Registro não encontrado.", 409: "Conflito com regras de negócio.",
        };
        const msg = (body && typeof body === "object" && body.message) || fallback[res.status] || `Erro ${res.status}`;
        throw new ApiError(msg, res.status, body);
    }
    return body as T;
}

export const api = {
    async login(username: string, password: string): Promise<Session> {
        const { token } = await request<{ token: string }>("/api/v1/authenticate", {
            method: "POST", body: JSON.stringify({ username, password }),
        }, undefined);
        const user = await request<AuthUser>("/api/v1/auth/me", {}, token);
        const session = { token, user };
        localStorage.setItem(KEY_SESSION, JSON.stringify(session));
        return session;
    },
    register: (data: RegisterRequest) =>
        request<{ userId: string; customerId: string }>("/api/v1/register", {
            method: "POST", body: JSON.stringify(data),
        }, undefined),
    activities: (active?: boolean) =>
        request<Activity[]>(`/api/v1/activity-classes${active === undefined ? "" : `?active=${active}`}`),
    activity: (id: string) => request<Activity>(`/api/v1/activity-classes/${id}`),
    createActivity: (data: ActivityRequest) =>
        request<Activity>("/api/v1/activity-classes", { method: "POST", body: JSON.stringify(data) }),
    updateActivity: (id: string, data: ActivityRequest) =>
        request<Activity>(`/api/v1/activity-classes/${id}`, { method: "PUT", body: JSON.stringify(data) }),
    sports: () => request<Sport[]>("/api/v1/sports"),
    rooms: () => request<Room[]>("/api/v1/rooms"),
    trainers: () => request<Trainer[]>("/api/v1/trainers"),
    report: (p: { startDate: string; endDate: string; sportId?: string | undefined; dayOfWeek?: string | undefined }) => {
        const q = new URLSearchParams({ startDate: p.startDate, endDate: p.endDate });
        if (p.sportId) q.set("sportId", p.sportId);
        if (p.dayOfWeek) q.set("dayOfWeek", p.dayOfWeek);
        return request<Report>(`/api/v1/activity-reports?${q}`);
    },
    myEnrollment: async (): Promise<Enrollment | null> => {
        try { return await request<Enrollment>("/api/v1/me/enrollment"); }
        catch (e) { if (e instanceof ApiError && e.status === 404) return null; throw e; }
    },
    enroll: (activityClassIds: string[]) =>
        request<Enrollment>("/api/v1/me/enrollment", { method: "POST", body: JSON.stringify({ activityClassIds }) }),
    cancel: (enrollmentActivityId: string, endDate: string) =>
        request<Enrollment>(`/api/v1/me/enrollment/activities/${enrollmentActivityId}?endDate=${endDate}`, { method: "DELETE" }),
};

export const brl = (v: number) =>
    new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(Number(v) || 0);
export const hhmm = (t: string) => (t || "").slice(0, 5);
export const days = (w: Weekday[]) =>
    WEEKDAYS.filter((d) => w?.includes(d.value)).map((d) => d.short).join(" · ");
export const dateBR = (d?: string | null) => (d ? d.split("-").reverse().join("/") : "—");
