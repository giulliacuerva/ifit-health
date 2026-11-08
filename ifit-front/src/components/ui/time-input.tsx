import { useState } from "react";
import { cn } from "@/lib/utils";

const HOURS = Array.from({ length: 24 }, (_, i) => String(i).padStart(2, "0"));
const MINUTES = Array.from({ length: 12 }, (_, i) => String(i * 5).padStart(2, "0"));

const selectClass =
  "h-9 rounded-md border border-input bg-background px-2 text-sm text-foreground shadow-xs outline-none transition-colors focus:border-ring focus:ring-2 focus:ring-ring/30 disabled:cursor-not-allowed disabled:opacity-50";


export function TimeInput({
  value,
  onChange,
  disabled,
  className,
}: {
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
  className?: string;
}) {
  const [initialH = "", initialM = ""] = (value || "").split(":");
  const [hh, setHh] = useState(initialH);
  const [mm, setMm] = useState(MINUTES.includes(initialM) ? initialM : "");

  const update = (h: string, m: string) => {
    setHh(h);
    setMm(m);
    onChange(h && m ? `${h}:${m}` : "");
  };

  return (
    <div className={cn("flex items-center gap-1", className)}>
      <select
        aria-label="Hora"
        className={selectClass}
        value={hh}
        disabled={disabled}
        onChange={(e) => update(e.target.value, mm)}
      >
        <option value="" disabled>
          --
        </option>
        {HOURS.map((h) => (
          <option key={h} value={h}>
            {h}
          </option>
        ))}
      </select>
      <span className="text-muted-foreground">:</span>
      <select
        aria-label="Minuto"
        className={selectClass}
        value={mm}
        disabled={disabled}
        onChange={(e) => update(hh, e.target.value)}
      >
        <option value="" disabled>
          --
        </option>
        {MINUTES.map((m) => (
          <option key={m} value={m}>
            {m}
          </option>
        ))}
      </select>
    </div>
  );
}
