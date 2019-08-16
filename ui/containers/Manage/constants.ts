import { ComplianceContent } from '../../models/Manage';
import { ComplianceType } from '../../constants';

export enum ManagePage {
  ComplianceTab = 'ComplianceTab',
}

export enum OpenType {
  Add = 'add',
  Update = 'update',
}

export const mockCompliances: ComplianceContent[] = [
  {
    name: ComplianceType.PCI,
    id: 100,
    description: 'Abstraction is often one floor above you.',
    questions: [
      {
        question: 'Lorem amet voluptate excepteur culpa laboris ea culpa mollit nostrud sunt consectetur nostrud?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'What do you do to unwind?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'What will be the future of TV shows?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
    ],
  },
  {
    name: ComplianceType.PHI,
    id: 101,
    description: 'The clock within this blog and the clock on my laptop are 1 hour different from each other.',
    questions: [
      {
        question: 'Full or What do you really wish you knew when you were younger?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'How far should governments go to prevent its citizens from causing harm to themselves?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'What was the best compliment you’ve received?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Is technological progress inevitable as long as humans exist or can it be stopped?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'An epic feast is held in your honor, what’s on the table?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Where would your friends or family be most surprised to find you?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'What in life is truly objective and not subjective?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
    ],
  },
  {
    name: ComplianceType.PHI,
    id: 103,
    description: 'Exercitation tempor consectetur non esse quis ut cupidatat ut.',
    questions: [
      {
        question: 'Sint adipisicing id dolore culpa aliquip elit minim laborum adipisicing excepteur dolore do?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Ad aliquip nulla tempor laboris tempor?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Aute ipsum ipsum deserunt reprehenderit laboris id elit mollit aliqua?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Duis deserunt ex Lorem magna consectetur officia in nulla pariatur officia cupidatat irure?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Ex veniam reprehenderit cillum cupidatat voluptate elit nulla labore?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Aute voluptate irure eiusmod elit irure pariatur culpa sint?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Enim dolor sunt minim minim nulla nulla deserunt veniam in mollit minim minim?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
    ],
  },
  {
    name: ComplianceType.PCI,
    id: 104,
    description: 'Voluptate quis et magna laborum ex Lorem.',
    questions: [
      {
        question: 'Sint adipisicing id dolore culpa aliquip elit minim laborum adipisicing excepteur dolore do?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Ad aliquip nulla tempor laboris tempor?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Aute ipsum ipsum deserunt reprehenderit laboris id elit mollit aliqua?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Duis deserunt ex Lorem magna consectetur officia in nulla pariatur officia cupidatat irure?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Ex veniam reprehenderit cillum cupidatat voluptate elit nulla labore?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Aute voluptate irure eiusmod elit irure pariatur culpa sint?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Enim dolor sunt minim minim nulla nulla deserunt veniam in mollit minim minim?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
    ],
  },
  {
    name: ComplianceType.PHI,
    id: 105,
    description: 'Irure aute anim non quis fugiat id aliquip ut sunt ad fugiat proident.',
    questions: [
      {
        question: 'Sint adipisicing id dolore culpa aliquip elit minim laborum adipisicing excepteur dolore do?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Ad aliquip nulla tempor laboris tempor?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Aute ipsum ipsum deserunt reprehenderit laboris id elit mollit aliqua?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Duis deserunt ex Lorem magna consectetur officia in nulla pariatur officia cupidatat irure?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Ex veniam reprehenderit cillum cupidatat voluptate elit nulla labore?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Aute voluptate irure eiusmod elit irure pariatur culpa sint?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Enim dolor sunt minim minim nulla nulla deserunt veniam in mollit minim minim?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Sint adipisicing id dolore culpa aliquip elit minim laborum adipisicing excepteur dolore do?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Ad aliquip nulla tempor laboris tempor?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Aute ipsum ipsum deserunt reprehenderit laboris id elit mollit aliqua?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Duis deserunt ex Lorem magna consectetur officia in nulla pariatur officia cupidatat irure?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Ex veniam reprehenderit cillum cupidatat voluptate elit nulla labore?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Aute voluptate irure eiusmod elit irure pariatur culpa sint?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Enim dolor sunt minim minim nulla nulla deserunt veniam in mollit minim minim?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Sint adipisicing id dolore culpa aliquip elit minim laborum adipisicing excepteur dolore do?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Ad aliquip nulla tempor laboris tempor?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Aute ipsum ipsum deserunt reprehenderit laboris id elit mollit aliqua?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Duis deserunt ex Lorem magna consectetur officia in nulla pariatur officia cupidatat irure?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Ex veniam reprehenderit cillum cupidatat voluptate elit nulla labore?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Aute voluptate irure eiusmod elit irure pariatur culpa sint?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Enim dolor sunt minim minim nulla nulla deserunt veniam in mollit minim minim?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
    ],
  },
  {
    name: ComplianceType.PHI,
    id: 106,
    description: 'Irure sunt sunt occaecat do minim do amet dolore.',
    questions: [
      {
        question: 'Sint adipisicing id dolore culpa aliquip elit minim laborum adipisicing excepteur dolore do?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Ad aliquip nulla tempor laboris tempor?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Aute ipsum ipsum deserunt reprehenderit laboris id elit mollit aliqua?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Duis deserunt ex Lorem magna consectetur officia in nulla pariatur officia cupidatat irure?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Ex veniam reprehenderit cillum cupidatat voluptate elit nulla labore?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Aute voluptate irure eiusmod elit irure pariatur culpa sint?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Enim dolor sunt minim minim nulla nulla deserunt veniam in mollit minim minim?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
    ],
  },
  {
    name: ComplianceType.PII,
    id: 107,
    description: 'Elit adipisicing ex aute esse est ea magna in in consequat aute officia.',
    questions: [
      {
        question: 'Sint adipisicing id dolore culpa aliquip elit minim laborum adipisicing excepteur dolore do?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Ad aliquip nulla tempor laboris tempor?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Aute ipsum ipsum deserunt reprehenderit laboris id elit mollit aliqua?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Duis deserunt ex Lorem magna consectetur officia in nulla pariatur officia cupidatat irure?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Ex veniam reprehenderit cillum cupidatat voluptate elit nulla labore?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Aute voluptate irure eiusmod elit irure pariatur culpa sint?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
      {
        question: 'Enim dolor sunt minim minim nulla nulla deserunt veniam in mollit minim minim?',
        requester: 'Ruud',
        date: new Date('2019-08-13'),
      },
    ],
  },
];
