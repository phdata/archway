export const TOPIC_FORM_CHANGED = 'TOPIC_FORM_CHANGED';

export function topicFormChanged(field) {
  return {
    type: TOPIC_FORM_CHANGED,
    field,
  };
}

export const CREATE_TOPIC = 'CREATE_TOPIC';

export function createTopic(database, suffix, partitions, replicationFactor) {
  return {
    type: CREATE_TOPIC,
    database,
    suffix,
    partitions,
    replicationFactor,
  };
}

export const GET_ALL_TOPICS = 'GET_ALL_TOPICS';

export function getAllTopics() {
  return {
    type: GET_ALL_TOPICS,
  }
}