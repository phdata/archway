export const APPLICATION_FORM_CHANGED = 'APPLICATION_FORM_CHANGED';

export function applicationFormChanged(field) {
  return {
    type: APPLICATION_FORM_CHANGED,
    field,
  };
}

export const CREATE_APPLICATION = 'CREATE_APPLICATION';

export function createApplication() {
  return {
    type: CREATE_APPLICATION,
  };
}

export const APPLICATION_CREATED = 'APPLICATION_CREATED';

export function applicationCreated() {
  return {
    type: APPLICATION_CREATED,
  };
}