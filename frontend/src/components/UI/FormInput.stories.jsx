import FormInput from './FormInput';

export default {
  title: 'UI/FormInput',
  component: FormInput,
};

export const Default = {
  args: {
    label: 'Email Address',
    placeholder: 'Enter your email',
    dark: false,
  },
};

export const DarkMode = {
  args: {
    label: 'Password',
    type: 'password',
    placeholder: 'Enter password',
    dark: true,
  },
  parameters: {
    backgrounds: { default: 'dark' }
  }
};
