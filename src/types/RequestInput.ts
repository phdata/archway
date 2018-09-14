import { Compliance } from "./Workspace";

export interface RequestInput {
  name: string
  summary: string
  description: string
  compliance: Compliance
}