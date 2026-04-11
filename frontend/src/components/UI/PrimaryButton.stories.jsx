import PrimaryButton from './PrimaryButton';

export default {
  title: 'UI/PrimaryButton',
  component: PrimaryButton,
  argTypes: {
    variant: {
      control: { type: 'select' },
      options: ['primary', 'secondary', 'glass', 'night', 'outline'],
    },
    disabled: { control: 'boolean' },
    loading: { control: 'boolean' },
  },
};

export const Primary = {
  args: {
    children: 'Get Started',
    variant: 'primary',
  },
};

export const Secondary = {
  args: {
    children: 'Cancel',
    variant: 'secondary',
  },
};

export const Glass = {
  args: {
    children: 'Transparent Glass Button',
    variant: 'glass',
  },
  parameters: {
    backgrounds: { default: 'dark' }
  }
};

export const Loading = {
  args: {
    children: 'Submit',
    loading: true,
  },
};
