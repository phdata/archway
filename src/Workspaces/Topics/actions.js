export const TOPIC_FORM_CHANGED = 'TOPIC_FORM_CHANGED';

export function topicFormChanged(field) {
  return {
    type: TOPIC_FORM_CHANGED,
    field,
  };
}

export const CREATE_TOPIC = 'CREATE_TOPIC';

export function createTopic() {
  return {
    type: CREATE_TOPIC,
  };
}

export const TOPIC_CREATED = 'TOPIC_CREATED';

export function topicCreated() {
  return {
    type: TOPIC_CREATED,
  };
}