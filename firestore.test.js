const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write user tasks", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("tasks").get());
});

test("Owner can list and write valid task and debt with timestamp", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const validTimestamp = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").doc("task_1").set({
      title: "Review Modul Bahasa",
      category: "STUDY",
      priority: "HIGH",
      dueDate: "2026-11-15",
      isCompleted: false,
      updatedAt: validTimestamp,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("debts").doc("debt_1").set({
      type: "DEBT",
      personName: "Budi",
      amount: 500000,
      paidAmount: 200000,
      isSettled: false,
      dueDate: "2026-12-01",
      updatedAt: validTimestamp,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("tasks").get()
  );
});

test("Cross-user access to another user's tasks or debts fails", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("tasks").doc("task_bob").set({
      title: "Secret Task",
      category: "WORK",
      priority: "HIGH",
      dueDate: "2026-11-16",
      isCompleted: false,
      updatedAt: new Date(Date.now() - 1000),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("tasks").doc("task_bob").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("tasks").get());
});

test("Rejects debt payload when paidAmount exceeds amount", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("debts").doc("debt_invalid").set({
      type: "DEBT",
      personName: "Budi",
      amount: 100000,
      paidAmount: 150000,
      isSettled: false,
      dueDate: "2026-12-01",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});
