import { ComplianceContent } from '../../models/Manage';
import { ComplianceType } from '../../constants';

export enum ManagePage {
  ComplianceTab = 'ComplianceTab',
}

export enum OpenType {
  Add = 'add',
  Update = 'update',
  Delete = 'delete',
}

export const mockCompliances: ComplianceContent[] = [
  {
    name: ComplianceType.PCI,
    description: 'Abstraction is often one floor above you.',
    id: 100,
    questions: [
      {
        question: 'Lorem amet voluptate excepteur culpa laboris ea culpa mollit nostrud sunt consectetur nostrud?',
        requester: 'Ruud',
        updated: new Date('2019-08-13'),
        id: 1,
        complianceGroupId: 100,
      },
      {
        question: 'What do you do to unwind?',
        requester: 'Ruud',
        updated: new Date('2019-08-13'),
        id: 2,
        complianceGroupId: 100,
      },
      {
        question: 'What will be the future of TV shows?',
        requester: 'Ruud',
        updated: new Date('2019-08-13'),
        id: 3,
        complianceGroupId: 100,
      },
    ],
  },
  {
    name: ComplianceType.PHI,
    description: 'The clock within this blog and the clock on my laptop are 1 hour different from each other.',
    id: 101,
    questions: [
      {
        question: 'Full or What do you really wish you knew when you were younger?',
        requester: 'Ruud',
        updated: new Date('2019-08-13'),
        id: 1,
        complianceGroupId: 101,
      },
      {
        question: 'How far should governments go to prevent its citizens from causing harm to themselves?',
        requester: 'Ruud',
        updated: new Date('2019-08-13'),
        id: 2,
        complianceGroupId: 101,
      },
      {
        question: 'What was the best compliment you’ve received?',
        requester: 'Ruud',
        updated: new Date('2019-08-13'),
        id: 3,
        complianceGroupId: 101,
      },
      {
        question: 'Is technological progress inevitable as long as humans exist or can it be stopped?',
        requester: 'Ruud',
        updated: new Date('2019-08-13'),
        id: 4,
        complianceGroupId: 101,
      },
      {
        question: 'An epic feast is held in your honor, what’s on the table?',
        requester: 'Ruud',
        updated: new Date('2019-08-13'),
        id: 5,
        complianceGroupId: 101,
      },
      {
        question: 'Where would your friends or family be most surprised to find you?',
        requester: 'Ruud',
        updated: new Date('2019-08-13'),
        id: 6,
        complianceGroupId: 101,
      },
      {
        question: 'What in life is truly objective and not subjective?',
        requester: 'Ruud',
        updated: new Date('2019-08-13'),
        id: 7,
        complianceGroupId: 101,
      },
    ],
  },
];
