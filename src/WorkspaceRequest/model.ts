import { Compliance } from "../WorkspaceListing/Workspace";

interface RequestInput {
  name: string
  summary: string
  description: string
  compliance: Compliance
}

export {
  RequestInput,
}