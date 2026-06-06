declare module '*.module.scss' {
    const classes: { [key: string]: string };
    export default classes;
}

declare module '*.scss';
declare module '*.css';

declare module '*.svg?react' {
    import { FC, SVGProps } from 'react';

    const ReactComponent: FC<SVGProps<SVGSVGElement>>;
    export default ReactComponent;
}
