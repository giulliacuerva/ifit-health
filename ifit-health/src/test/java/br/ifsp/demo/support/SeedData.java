package br.ifsp.demo.support;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public final class SeedData {
    public static final String ADMIN_EMAIL = "admin@ifit.com";
    public static final String ADMIN_PASSWORD = "admin";
    public static final UUID ADMIN_USER_ID = UUID.fromString("18e0391c-db2e-4180-be92-bdd5badd11dd");
    public static final UUID ADMIN_CUSTOMER_ID = UUID.fromString("aa4979e8-b32c-491a-bc65-28cc71eeeeba");

    public static final UUID ANA_USER_ID = UUID.fromString("65c96008-1905-4939-b96d-781ed566bf0e");
    public static final UUID ANA_CUSTOMER_ID = UUID.fromString("10d4ef92-04ae-4a09-b0f6-b59dfdf4c01a");
    public static final String ANA_EMAIL = "ana.souza@example.com";
    public static final String ANA_CPF = "52998224725";

    public static final UUID SPORT_SWIMMING = UUID.fromString("932dc1dc-fa6e-48c0-89f5-4d9e8a61a4ba");
    public static final UUID SPORT_WEIGHT_TRAINING = UUID.fromString("f0094ec9-5c8f-4e0e-9f42-211d00f7eb09");
    public static final UUID SPORT_FUNCTIONAL = UUID.fromString("67e18dc7-396b-4d98-b189-92875fb85fd3");
    public static final UUID SPORT_JUDO = UUID.fromString("31728ab5-c0cc-4e5c-af88-8db87e83f5e1");
    public static final UUID SPORT_SOCCER = UUID.fromString("dcbb1518-5669-43c9-8d84-ff22ef802579");
    public static final UUID SPORT_VOLLEYBALL = UUID.fromString("93c06615-7de9-41b3-a15c-068b87349a91");
    public static final UUID SPORT_ATHLETICS = UUID.fromString("8c3e0b65-8bd8-4df3-8749-2277ce608d5c");

    public static final UUID ROOM_POOL = UUID.fromString("81ed8aab-deda-4a43-8687-c86d2e9cb236");
    public static final UUID ROOM_GYM = UUID.fromString("8a069a4b-4fef-4957-bfa6-098d38975c3f");
    public static final UUID ROOM_FUNCTIONAL = UUID.fromString("34e3460f-1384-47ef-9396-0b575acc8b65");
    public static final UUID ROOM_JUDO = UUID.fromString("df8a45de-4c39-4a30-aa09-74d71f5266ed");
    public static final UUID ROOM_SOCCER_COURT = UUID.fromString("b9873518-3b23-40f6-9e4e-799fd605a17b");
    public static final UUID ROOM_VOLLEYBALL_COURT = UUID.fromString("1eb4eda5-f66b-460b-bf06-03db323c86cb");
    public static final UUID ROOM_TRACK = UUID.fromString("de93f947-a80d-4279-b995-69cf3c7969c7");

    public static final int ROOM_POOL_CAPACITY = 20;
    public static final int ROOM_GYM_CAPACITY = 30;

    public static final UUID TRAINER_JOHN = UUID.fromString("5447f51f-6f94-40ea-8a43-bcb40ed43fb8");
    public static final UUID TRAINER_MARIA = UUID.fromString("98d5ee7a-0830-4573-89c0-40005cb6c525");
    public static final UUID TRAINER_CARLOS = UUID.fromString("d1b625b3-01f9-4513-b7e5-395fe6fc30fc");
    public static final UUID TRAINER_JULIANA = UUID.fromString("87a7f402-872a-4d88-9225-e774415f69b9");
    public static final String TRAINER_JULIANA_NAME = "Juliana Costa";

    public static final int SPORTS_COUNT = 8;
    public static final int ROOMS_COUNT = 8;
    public static final int TRAINERS_COUNT = 8;

    public static final UUID SWIMMING_CLASS = UUID.fromString("5856c456-bf5e-45bf-b1ec-ce5697ea109b");
    public static final String SWIMMING_NAME = "Natação";
    public static final DayOfWeek SWIMMING_DAY = DayOfWeek.MONDAY;
    public static final LocalTime SWIMMING_START = LocalTime.of(11, 30);
    public static final LocalTime SWIMMING_END = LocalTime.of(12, 30);
    public static final int SWIMMING_CAPACITY = 20;
    public static final BigDecimal SWIMMING_FEE = new BigDecimal("180");

    private SeedData() {
    }
}
